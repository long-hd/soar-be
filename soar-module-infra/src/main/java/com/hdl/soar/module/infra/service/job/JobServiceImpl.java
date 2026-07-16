package com.hdl.soar.module.infra.service.job;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.framework.quartz.core.handler.JobHandler;
import com.hdl.soar.framework.quartz.core.scheduler.SchedulerManager;
import com.hdl.soar.framework.quartz.core.util.CronUtils;
import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.job.dto.job.JobSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.job.JobPO;
import com.hdl.soar.module.infra.dal.entity.job.JobPO_;
import com.hdl.soar.module.infra.dal.postgres.job.JobRepository;
import com.hdl.soar.module.infra.enums.job.JobStatusEnum;
import com.hdl.soar.module.infra.mapper.job.JobMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.eqIfPresent;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.likeIfPresent;
import static com.hdl.soar.module.infra.enums.ErrorCodeConstants.*;

/**
 * Keeps job config ({@code infra_job}) and the live Quartz schedule in step.
 *
 * <p>Every mutating method is transactional. Spring's LocalDataSourceJobStore uses the same
 * DataSource and transaction manager, so the Quartz write and the config write commit or
 * roll back together — the two stores cannot silently diverge on a failed request.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobServiceImpl implements JobService {

    JobRepository jobRepository;
    SchedulerManager schedulerManager;
    ApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(JobSaveReqDTO createReqDTO) throws SchedulerException {
        validateCronExpression(createReqDTO.getCronExpression());
        // 1.1 handlerName is the job's identity — it must be free
        if (jobRepository.findByHandlerName(createReqDTO.getHandlerName()).isPresent()) {
            throw exception(JOB_HANDLER_EXISTS);
        }
        // 1.2 refuse to schedule a handler that doesn't exist, rather than fail at fire time
        validateJobHandlerExists(createReqDTO.getHandlerName());

        // 2. save as INIT: registered in config, not yet handed to the scheduler
        JobPO job = JobMapper.INSTANCE.toPO(createReqDTO);
        job.setStatus(JobStatusEnum.INIT);
        jobRepository.save(job);

        // 3. hand it to Quartz, then mark it live
        schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(),
                job.getCronExpression(), job.getRetryCount(), job.getRetryInterval());
        job.setStatus(JobStatusEnum.NORMAL);
        jobRepository.save(job);
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(JobSaveReqDTO updateReqDTO) throws SchedulerException {
        validateCronExpression(updateReqDTO.getCronExpression());
        JobPO job = validateJobExists(updateReqDTO.getId());
        // Only an enabled job may be edited: rescheduling a paused job would silently resume it
        if (!Objects.equals(job.getStatus(), JobStatusEnum.NORMAL)) {
            throw exception(JOB_UPDATE_ONLY_NORMAL_STATUS);
        }

        JobMapper.INSTANCE.updatePO(updateReqDTO, job);
        jobRepository.save(job);

        schedulerManager.updateJob(job.getHandlerName(), updateReqDTO.getHandlerParam(),
                updateReqDTO.getCronExpression(), updateReqDTO.getRetryCount(),
                updateReqDTO.getRetryInterval());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJobStatus(Long id, Integer status) throws SchedulerException {
        // INIT is internal — callers may only enable or pause
        if (!Objects.equals(status, JobStatusEnum.NORMAL.getStatus())
                && !Objects.equals(status, JobStatusEnum.STOP.getStatus())) {
            throw exception(JOB_CHANGE_STATUS_INVALID);
        }
        JobPO job = validateJobExists(id);
        if (Objects.equals(job.getStatus().getStatus(), status)) {
            throw exception(JOB_CHANGE_STATUS_EQUALS);
        }

        job.setStatus(JobStatusEnum.of(status));
        jobRepository.save(job);

        if (Objects.equals(status, JobStatusEnum.NORMAL.getStatus())) {
            schedulerManager.resumeJob(job.getHandlerName());
        } else {
            schedulerManager.pauseJob(job.getHandlerName());
        }
    }

    @Override
    public void triggerJob(Long id) throws SchedulerException {
        JobPO job = validateJobExists(id);
        schedulerManager.triggerJob(job.getId(), job.getHandlerName(), job.getHandlerParam());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncJob() throws SchedulerException {
        for (JobPO job : jobRepository.findAll()) {
            // Delete then re-add: simpler than reconciling, and idempotent
            schedulerManager.deleteJob(job.getHandlerName());
            schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(),
                    job.getCronExpression(), job.getRetryCount(), job.getRetryInterval());
            if (Objects.equals(job.getStatus(), JobStatusEnum.STOP)) {
                schedulerManager.pauseJob(job.getHandlerName());
            }
            log.info("[syncJob][id({}) handlerName({}) synced]", job.getId(), job.getHandlerName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long id) throws SchedulerException {
        JobPO job = validateJobExists(id);
        jobRepository.delete(job);
        schedulerManager.deleteJob(job.getHandlerName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobList(List<Long> ids) throws SchedulerException {
        List<JobPO> jobs = jobRepository.findAllById(ids);
        jobRepository.deleteAll(jobs);
        for (JobPO job : jobs) {
            schedulerManager.deleteJob(job.getHandlerName());
        }
    }

    @Override
    public JobPO getJob(Long id) {
        return jobRepository.findById(id).orElse(null);
    }

    @Override
    public PageResult<JobPO> getJobPage(JobPageReqDTO pageReqDTO) {
        Specification<JobPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, JobPO_.name, pageReqDTO.getName());
            likeIfPresent(predicates, cb, root, JobPO_.handlerName, pageReqDTO.getHandlerName());
            eqIfPresent(predicates, cb, root, JobPO_.status, JobStatusEnum.of(pageReqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<JobPO> page = jobRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, JobPO_.ID)));
        return PageUtils.toPageResult(page);
    }

    // ============ Helper

    private JobPO validateJobExists(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> exception(JOB_NOT_EXISTS));
    }

    private void validateCronExpression(String cronExpression) {
        if (!CronUtils.isValid(cronExpression)) {
            throw exception(JOB_CRON_EXPRESSION_VALID);
        }
    }

    /**
     * The handler must be a live bean implementing the JobHandler contract — otherwise the
     * job would be accepted here and only blow up later, at fire time.
     */
    private void validateJobHandlerExists(String handlerName) {
        Object handler;
        try {
            handler = applicationContext.getBean(handlerName);
        } catch (NoSuchBeanDefinitionException ex) {
            throw exception(JOB_HANDLER_BEAN_NOT_EXISTS);
        }
        if (!(handler instanceof JobHandler)) {
            throw exception(JOB_HANDLER_BEAN_TYPE_ERROR);
        }
    }

}
