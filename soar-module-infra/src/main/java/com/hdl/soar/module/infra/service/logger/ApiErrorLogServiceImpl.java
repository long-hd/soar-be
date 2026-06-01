package com.hdl.soar.module.infra.service.logger;


import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.string.StrUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.infra.controller.admin.logger.dto.apierrorlog.ApiErrorLogPageReqDTO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiErrorLogPO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiErrorLogPO_;
import com.hdl.soar.module.infra.dal.postgres.logger.ApiErrorLogRepository;
import com.hdl.soar.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import com.hdl.soar.module.infra.mapper.logger.ApiErrorLogMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;
import static com.hdl.soar.module.infra.dal.entity.logger.ApiErrorLogPO.REQUEST_PARAMS_MAX_LENGTH;
import static com.hdl.soar.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_NOT_FOUND;
import static com.hdl.soar.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_PROCESSED;

@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiErrorLogServiceImpl implements ApiErrorLogService {

    ApiErrorLogRepository apiErrorLogRepository;

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        ApiErrorLogPO po = ApiErrorLogMapper.INSTANCE.toPO(createDTO);
        po.setRequestParams(StrUtils.maxLength(po.getRequestParams(), REQUEST_PARAMS_MAX_LENGTH));
        po.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT);
        apiErrorLogRepository.save(po);
    }

    @Override
    public ApiErrorLogPO getApiErrorLog(Long id) {
        return apiErrorLogRepository.findById(id).orElse(null);
    }

    @Override
    public PageResult<ApiErrorLogPO> getApiErrorLogPage(ApiErrorLogPageReqDTO pageReqDTO) {
        Specification<ApiErrorLogPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, ApiErrorLogPO_.userId, pageReqDTO.getUserId());
            eqIfPresent(predicates, cb, root, ApiErrorLogPO_.userType, UserTypeEnum.of(pageReqDTO.getUserType()));
            likeIfPresent(predicates, cb, root, ApiErrorLogPO_.applicationName, pageReqDTO.getApplicationName());
            likeIfPresent(predicates, cb, root, ApiErrorLogPO_.requestUrl, pageReqDTO.getRequestUrl());
            betweenIfPresent(predicates, cb, root, ApiErrorLogPO_.exceptionTime, pageReqDTO.getExceptionTime());
            eqIfPresent(predicates, cb, root, ApiErrorLogPO_.processStatus,
                    ApiErrorLogProcessStatusEnum.of(pageReqDTO.getProcessStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ApiErrorLogPO> page = apiErrorLogRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, ApiErrorLogPO_.ID)));
        return PageUtils.toPageResult(page);
    }

    @Override
    public void updateApiErrorLogProcess(Long id, Integer processStatus, Long processUserId) {
        // 1. Validate exists
        ApiErrorLogPO errorLog = apiErrorLogRepository.findById(id)
                .orElseThrow(() -> exception(API_ERROR_LOG_NOT_FOUND));

        // 2. Validate not already processed
        if (!ApiErrorLogProcessStatusEnum.INIT.equals(errorLog.getProcessStatus())) {
            throw exception(API_ERROR_LOG_PROCESSED);
        }

        // 3. Update
        errorLog.setProcessStatus(ApiErrorLogProcessStatusEnum.of(processStatus));
        errorLog.setProcessTime(Instant.now());
        errorLog.setProcessUserId(processUserId);
        apiErrorLogRepository.save(errorLog);
    }

    @Override
    public Integer cleanErrorLog(Integer exceedDay, Integer deleteLimit) {
        return 0;
    }

}
