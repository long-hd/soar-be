package com.hdl.soar.module.system.service.logger;

import com.hdl.soar.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.system.controller.admin.logger.dto.operatelog.OperateLogPageReqDTO;
import com.hdl.soar.module.system.dal.entity.logger.OperateLogPO;
import com.hdl.soar.module.system.dal.entity.logger.OperateLogPO_;
import com.hdl.soar.module.system.dal.postgres.logger.OperateLogRepository;
import com.hdl.soar.module.system.mapper.logger.OperateLogMapper;
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

@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OperateLogServiceImpl implements OperateLogService {

    OperateLogRepository operateLogRepository;

    @Override
    public void createOperateLog(OperateLogCreateReqDTO createDTO) {
        OperateLogPO po = OperateLogMapper.INSTANCE.toPO(createDTO);
        operateLogRepository.save(po);
    }

    @Override
    public OperateLogPO getOperateLog(Long id) {
        return operateLogRepository.findById(id).orElse(null);
    }

    @Override
    public PageResult<OperateLogPO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO) {
        Specification<OperateLogPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, OperateLogPO_.userId, pageReqDTO.getUserId());
            likeIfPresent(predicates, cb, root, OperateLogPO_.module, pageReqDTO.getModule());
            likeIfPresent(predicates, cb, root, OperateLogPO_.name, pageReqDTO.getName());
            eqIfPresent(predicates, cb, root, OperateLogPO_.bizId, pageReqDTO.getBizId());
            likeIfPresent(predicates, cb, root, OperateLogPO_.content, pageReqDTO.getContent());
            betweenIfPresent(predicates, cb, root, OperateLogPO_.createTime, pageReqDTO.getCreateTime());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<OperateLogPO> page = operateLogRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, OperateLogPO_.ID)));
        return PageUtils.toPageResult(page);
    }
}
