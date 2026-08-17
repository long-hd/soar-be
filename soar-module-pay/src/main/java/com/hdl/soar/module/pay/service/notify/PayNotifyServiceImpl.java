package com.hdl.soar.module.pay.service.notify;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import com.hdl.soar.module.pay.api.notify.PayOrderNotifyReqDTO;
import com.hdl.soar.module.pay.api.notify.PayRefundNotifyReqDTO;
import com.hdl.soar.module.pay.controller.admin.notify.dto.PayNotifyTaskPageReqDTO;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyLogPO;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyTaskPO;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyTaskPO_;
import com.hdl.soar.module.pay.dal.entity.order.PayOrderPO;
import com.hdl.soar.module.pay.dal.entity.refund.PayRefundPO;
import com.hdl.soar.module.pay.dal.postgres.notify.PayNotifyLogRepository;
import com.hdl.soar.module.pay.dal.postgres.notify.PayNotifyTaskRepository;
import com.hdl.soar.module.pay.dal.redis.notify.PayNotifyLockRedisDAO;
import com.hdl.soar.module.pay.enums.notify.PayNotifyStatusEnum;
import com.hdl.soar.module.pay.enums.notify.PayNotifyTypeEnum;
import com.hdl.soar.module.pay.framework.job.config.PayJobConfiguration;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.eqIfPresent;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.likeIfPresent;
import static com.hdl.soar.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;
import static com.hdl.soar.module.pay.enums.ErrorCodeConstants.NOTIFY_TASK_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayNotifyServiceImpl implements PayNotifyService {

    /** Backoff between attempts, in seconds. Length is {@code maxNotifyTimes - 1} (retries only). */
    private static final int[] NOTIFY_FREQUENCY = {15, 15, 30, 180, 1800, 1800, 1800, 3600};
    /** Initial attempt + {@link #NOTIFY_FREQUENCY}.length retries. */
    private static final int MAX_NOTIFY_TIMES = 9;
    /** Max seconds the poll job waits for its dispatched attempts before returning. */
    private static final long POLL_AWAIT_SECONDS = 30;

    PayNotifyTaskRepository taskRepository;
    PayNotifyLogRepository logRepository;
    PayNotifyLockRedisDAO lockRedisDAO;
    RestClient restClient;

    @Qualifier(PayJobConfiguration.NOTIFY_EXECUTOR)
    Executor executor;

    // ================ enqueue (outbox) ================

    @Override
    @Transactional(rollbackFor = Exception.class) // joins the caller's order-success transaction
    public void createPayNotifyTask(PayOrderPO order) {
        Instant now = Instant.now();
        PayNotifyTaskPO task = PayNotifyTaskPO.builder()
                .appId(order.getAppId())
                .type(PayNotifyTypeEnum.ORDER)
                .dataId(order.getId())
                .merchantOrderId(order.getMerchantOrderId())
                .notifyUrl(order.getNotifyUrl())
                .status(PayNotifyStatusEnum.WAITING)
                .nextNotifyTime(now)
                .notifyTimes(0)
                .maxNotifyTimes(MAX_NOTIFY_TIMES)
                .build();
        taskRepository.save(task); // same transaction as the caller -> true outbox, no dual write

        // Fast-path: fire the first attempt right after this transaction commits, so a healthy
        // merchant hears in ~milliseconds instead of waiting for the next poll. The poll job is the
        // backstop for when the app dies between commit and this callback.
        Long taskId = task.getId();
        Long tenantId = task.getTenantId(); // populated by @TenantId on insert
        registerAfterCommit(() -> executor.execute(() -> executeNotify0(taskId, tenantId)));
    }

    @Override
    public void createPayNotifyTask(PayRefundPO refund) {
        Instant now = Instant.now();
        PayNotifyTaskPO task = PayNotifyTaskPO.builder()
                .appId(refund.getAppId())
                .type(PayNotifyTypeEnum.REFUND)
                .dataId(refund.getId())
                .merchantOrderId(refund.getMerchantOrderId())
                .merchantRefundId(refund.getMerchantRefundId())
                .notifyUrl(refund.getNotifyUrl())
                .status(PayNotifyStatusEnum.WAITING)
                .nextNotifyTime(now)
                .notifyTimes(0)
                .maxNotifyTimes(MAX_NOTIFY_TIMES)
                .build();
        taskRepository.save(task);

        Long taskId = task.getId();
        Long tenantId = task.getTenantId();
        registerAfterCommit(() -> executor.execute(() -> executeNotify0(taskId, tenantId)));
    }

    private void registerAfterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
        } else {
            runnable.run(); // no transaction (e.g. a direct unit-test call): just run it
        }
    }

    // ================ relay (poll backstop) ================

    @Override
    public int executeNotify() {
        Instant now = Instant.now();
        List<PayNotifyTaskPO> tasks = taskRepository
                .findTop200ByStatusAndNextNotifyTimeLessThanEqualOrderByNextNotifyTimeAsc(
                        PayNotifyStatusEnum.WAITING, now);
        if (tasks.isEmpty()) {
            return 0;
        }
        Long tenantId = TenantContextHolder.getTenantId(); // set by @TenantJob
        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (PayNotifyTaskPO task : tasks) {
            Long taskId = task.getId();
            executor.execute(() -> {
                try {
                    executeNotify0(taskId, tenantId);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await(POLL_AWAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return tasks.size();
    }

    /**
     * Deliver one task. Runs on an executor thread that has no tenant/DB context of its own, so the
     * body is wrapped in the task's tenant and guarded by the per-task lock. The HTTP call is made
     * OUTSIDE any DB transaction; only the short result write ({@link #processNotifyResult}) is
     * transactional, so a slow merchant never holds a database transaction open.
     */
    private void executeNotify0(Long taskId, Long tenantId) {
        TenantUtils.execute(tenantId, () -> lockRedisDAO.lock(taskId, () -> {
            PayNotifyTaskPO task = taskRepository.findById(taskId).orElse(null);
            if (task == null || !PayNotifyStatusEnum.WAITING.equals(task.getStatus())) {
                return; // already terminal, or vanished
            }
            // No merchant callback configured -> nothing to deliver. End as SUCCESS no-op with a log
            // row, so the task doesn't sit WAITING forever or retry against a null url.
            if (task.getNotifyUrl() == null || task.getNotifyUrl().isBlank()) {
                getSelf().processNotifyResult(taskId, true, "skipped: no notify url");
                return;
            }
            NotifyResult result = requestMerchant(task);
            getSelf().processNotifyResult(taskId, result.success(), result.response());
        }));
    }

    /** POST the notify body to the merchant and interpret the {@link CommonResult} ack. */
    private NotifyResult requestMerchant(PayNotifyTaskPO task) {
        Object body = buildNotifyBody(task);
        try {
            CommonResult<?> result = restClient.post()
                    .uri(task.getNotifyUrl())
                    .header(HEADER_TENANT_ID, String.valueOf(task.getTenantId()))
                    .body(body)
                    .retrieve()
                    .body(CommonResult.class);
            boolean ok = result != null && result.isSuccess();
            String text = result == null ? "empty response" : ("code=" + result.getCode());
            return new NotifyResult(ok, StrUtil.maxLength(text, 1024));
        } catch (Exception ex) {
            log.warn("[executeNotify0][task({}) delivery failed]", task.getId(), ex);
            return new NotifyResult(false, StrUtil.maxLength(StrUtil.toStringOrNull(ex.getMessage()), 1024));
        }
    }

    /** Build the merchant notify body for this task's type. */
    private Object buildNotifyBody(PayNotifyTaskPO task) {
        if (PayNotifyTypeEnum.REFUND.equals(task.getType())) {
            PayRefundNotifyReqDTO b = new PayRefundNotifyReqDTO();
            b.setMerchantOrderId(task.getMerchantOrderId());
            b.setMerchantRefundId(task.getMerchantRefundId());
            b.setPayRefundId(task.getDataId());
            return b;
        }
        PayOrderNotifyReqDTO b = new PayOrderNotifyReqDTO();
        b.setMerchantOrderId(task.getMerchantOrderId());
        b.setPayOrderId(task.getDataId());
        return b;
    }

    /**
     * Record one attempt: bump the counter, write a log row, and either finish (SUCCESS), give up
     * (FAILURE at the ceiling), or schedule the next retry using the backoff schedule.
     */
    @Transactional(rollbackFor = Exception.class)
    public void processNotifyResult(Long taskId, boolean success, String response) {
        PayNotifyTaskPO task = taskRepository.findById(taskId).orElse(null);
        if (task == null || !PayNotifyStatusEnum.WAITING.equals(task.getStatus())) {
            return;
        }
        Instant now = Instant.now();
        int attempt = task.getNotifyTimes() + 1;
        task.setNotifyTimes(attempt);
        task.setLastExecuteTime(now);
        if (success) {
            task.setStatus(PayNotifyStatusEnum.SUCCESS);
        } else if (attempt >= task.getMaxNotifyTimes()) {
            task.setStatus(PayNotifyStatusEnum.FAILURE);
        } else {
            task.setNextNotifyTime(now.plusSeconds(NOTIFY_FREQUENCY[attempt - 1]));
        }
        taskRepository.save(task);

        logRepository.save(PayNotifyLogPO.builder()
                .taskId(taskId)
                .notifyTimes(attempt)
                .status(success ? PayNotifyStatusEnum.SUCCESS : PayNotifyStatusEnum.FAILURE)
                .response(response)
                .build());
    }

    // ================ query (admin) ================

    @Override
    public PayNotifyTaskPO getNotifyTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> exception(NOTIFY_TASK_NOT_FOUND));
    }

    @Override
    public PageResult<PayNotifyTaskPO> getNotifyTaskPage(PayNotifyTaskPageReqDTO pageReqDTO) {
        Specification<PayNotifyTaskPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, PayNotifyTaskPO_.appId, pageReqDTO.getAppId());
            eqIfPresent(predicates, cb, root, PayNotifyTaskPO_.status, PayNotifyStatusEnum.of(pageReqDTO.getStatus()));
            likeIfPresent(predicates, cb, root, PayNotifyTaskPO_.merchantOrderId, pageReqDTO.getMerchantOrderId());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageUtils.toPageable(pageReqDTO);
        Page<PayNotifyTaskPO> page = taskRepository.findAll(spec, pageable);
        return PageUtils.toPageResult(page);
    }

    @Override
    public List<PayNotifyLogPO> getNotifyLogList(Long taskId) {
        return logRepository.findAllByTaskIdOrderByNotifyTimesAsc(taskId);
    }


    // ================ helper ================

    /** Resolve the Spring-proxied self so {@code @Transactional} applies when called internally. */
    private PayNotifyServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    /** Outcome of one delivery attempt. */
    private record NotifyResult(boolean success, String response) {
    }

}
