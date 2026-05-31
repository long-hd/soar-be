package com.hdl.soar.module.infra.service.logger;

import com.hdl.soar.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.string.StrUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.infra.controller.admin.logger.dto.ApiAccessLogPageReqDTO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiAccessLogPO;
import com.hdl.soar.module.infra.dal.entity.logger.ApiAccessLogPO_;
import com.hdl.soar.module.infra.dal.postgres.logger.ApiAccessLogRepository;
import com.hdl.soar.module.infra.mapper.logger.ApiAccessLogMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import java.util.ArrayList;
import java.util.List;

import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;
import static com.hdl.soar.module.infra.dal.entity.logger.ApiAccessLogPO.REQUEST_PARAMS_MAX_LENGTH;
import static com.hdl.soar.module.infra.dal.entity.logger.ApiAccessLogPO.RESULT_MSG_MAX_LENGTH;

@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiAccessLogServiceImpl implements ApiAccessLogService {

    ApiAccessLogRepository apiAccessLogRepository;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        ApiAccessLogPO po = ApiAccessLogMapper.INSTANCE.toPO(createDTO);
        po.setRequestParams(StrUtils.maxLength(po.getRequestParams(), REQUEST_PARAMS_MAX_LENGTH));
        po.setResultMsg(StrUtils.maxLength(po.getResultMsg(), RESULT_MSG_MAX_LENGTH));
        apiAccessLogRepository.save(po);
    }

    @Override
    public ApiAccessLogPO getApiAccessLog(Long id) {
        return apiAccessLogRepository.findById(id).orElse(null);
    }

    @Override
    public PageResult<ApiAccessLogPO> getApiAccessLogPage(ApiAccessLogPageReqDTO pageReqDTO) {
        Specification<ApiAccessLogPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, ApiAccessLogPO_.userId, pageReqDTO.getUserId());
            eqIfPresent(predicates, cb, root, ApiAccessLogPO_.userType, UserTypeEnum.of(pageReqDTO.getUserType()));
            likeIfPresent(predicates, cb, root, ApiAccessLogPO_.applicationName, pageReqDTO.getApplicationName());
            likeIfPresent(predicates, cb, root, ApiAccessLogPO_.requestUrl, pageReqDTO.getRequestUrl());
            betweenIfPresent(predicates, cb, root, ApiAccessLogPO_.beginTime, pageReqDTO.getBeginTime());
            gteIfPresent(predicates, cb, root, ApiAccessLogPO_.duration, pageReqDTO.getDuration());
            eqIfPresent(predicates, cb, root, ApiAccessLogPO_.resultCode, pageReqDTO.getResultCode());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ApiAccessLogPO> page = apiAccessLogRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, ApiAccessLogPO_.ID)));
        return PageUtils.toPageResult(page);
    }

    @Override
    public Integer cleanAccessLog(Integer exceedDay, Integer deleteLimit) {
        return 0;
    }
}
