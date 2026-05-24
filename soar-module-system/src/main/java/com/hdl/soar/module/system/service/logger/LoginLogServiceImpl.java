package com.hdl.soar.module.system.service.logger;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.object.BeanUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.hdl.soar.module.system.controller.admin.logger.dto.loginlog.LoginLogPageReqDTO;
import com.hdl.soar.module.system.dal.entity.logger.LoginLogPO;
import com.hdl.soar.module.system.dal.postgres.logger.LoginLogRepository;
import com.hdl.soar.module.system.enums.logger.LoginResultEnum;
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

/**
 * Login Log Service Implementation
 */
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginLogServiceImpl implements LoginLogService{

    LoginLogRepository loginLogRepository;

    @Override
    public LoginLogPO getLoginLog(Long id) {
        return loginLogRepository.findById(id).orElse(null);
    }

    @Override
    public PageResult<LoginLogPO> getLoginLogPage(LoginLogPageReqDTO pageReqDTO) {
        Specification<LoginLogPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, LoginLogPO_.userIp, pageReqDTO.getUserIp());
            likeIfPresent(predicates, cb, root, LoginLogPO_.username, pageReqDTO.getUsername());
            betweenIfPresent(predicates, cb, root, LoginLogPO_.createTime, pageReqDTO.getCreateTime());
            if(Boolean.TRUE.equals(pageReqDTO.getStatus())) {
                predicates.add(cb.equal(root.get(LoginLogPO_.result), LoginResultEnum.SUCCESS.getResult()));
            } else if (Boolean.FALSE.equals(pageReqDTO.getStatus())) {
                predicates.add(cb.greaterThan(root.get(LoginLogPO_.result), LoginResultEnum.SUCCESS.getResult()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<LoginLogPO> page = loginLogRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, LoginLogPO_.ID)));
        return PageUtils.toPageResult(page);
    }

    @Override
    public void createLoginLog(LoginLogCreateReqDTO reqDTO) {
        LoginLogPO loginLog = BeanUtils.toBean(reqDTO, LoginLogPO.class);
        loginLogRepository.save(loginLog);
    }
}
