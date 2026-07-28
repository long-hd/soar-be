package com.hdl.soar.module.infra.service.job;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.infra.controller.admin.job.dto.log.JobLogPageReqDTO;
import com.hdl.soar.module.infra.dal.entity.job.JobLogPO;
import com.hdl.soar.module.infra.dal.entity.job.JobLogPO_;
import com.hdl.soar.module.infra.dal.postgres.job.JobLogRepository;
import com.hdl.soar.module.infra.enums.job.JobLogStatusEnum;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobLogServiceImpl implements JobLogService {

    JobLogRepository jobLogRepository;

    @Override
    public Long createJobLog(Long jobId, Instant beginTime, String handlerName, String handlerParam, Integer executeIndex) {
        JobLogPO log = JobLogPO.builder()
                .jobId(jobId).handlerName(handlerName).handlerParam(handlerParam)
                .executeIndex(executeIndex).beginTime(beginTime)
                .status(JobLogStatusEnum.RUNNING)
                .build();
        jobLogRepository.save(log);
        return log.getId();
    }

    @Override
    public void updateJobLogResult(Long logId, Instant endTime, Integer duration, boolean success, String result) {
        JobLogPO log = jobLogRepository.findById(logId).orElse(null);
        if (log == null) {
            // The start row may be absent (e.g. a manual trigger that skipped createJobLog);
            // there is nothing to complete, so just skip rather than fail the job.
            return;
        }
        log.setEndTime(endTime);
        log.setDuration(duration);
        log.setStatus(success ? JobLogStatusEnum.SUCCESS : JobLogStatusEnum.FAILURE);
        log.setResult(result);
        jobLogRepository.save(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer cleanJobLog(Integer exceedDay, Integer deleteLimit) {
        Instant expireTime = Instant.now().minus(exceedDay, ChronoUnit.DAYS);
        int count = 0;
        // Loop-delete in batches so one run never issues a single unbounded DELETE.
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleted = jobLogRepository.deleteByCreateTimeLtWithLimit(expireTime, deleteLimit);
            count += deleted;
            if (deleted < deleteLimit) { // fewer than a full batch -> nothing left
                break;
            }
        }
        return count;
    }

    @Override
    public JobLogPO getJobLog(Long id) {
        return jobLogRepository.findById(id).orElse(null);
    }

    @Override
    public PageResult<JobLogPO> getJobLogPage(JobLogPageReqDTO pageReqDTO) {
        Specification<JobLogPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, JobLogPO_.jobId, pageReqDTO.getJobId());
            likeIfPresent(predicates, cb, root, JobLogPO_.handlerName, pageReqDTO.getHandlerName());
            eqIfPresent(predicates, cb, root, JobLogPO_.status, JobLogStatusEnum.of(pageReqDTO.getStatus()));
            betweenIfPresent(predicates, cb, root, JobLogPO_.beginTime, pageReqDTO.getBeginTime());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<JobLogPO> page = jobLogRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, JobLogPO_.ID)));
        return PageUtils.toPageResult(page);
    }

}
