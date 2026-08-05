package com.hdl.soar.module.pay.service.app;

import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.app.dto.PayAppSaveReqDTO;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO;
import com.hdl.soar.module.pay.dal.entity.app.PayAppPO_;
import com.hdl.soar.module.pay.dal.postgres.app.PayAppRepository;
import com.hdl.soar.module.pay.mapper.app.PayAppMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.eqIfPresent;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.likeIfPresent;
import static com.hdl.soar.module.pay.enums.ErrorCodeConstants.APP_KEY_DUPLICATE;
import static com.hdl.soar.module.pay.enums.ErrorCodeConstants.APP_NOT_FOUND;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayAppServiceImpl implements PayAppService {

    PayAppRepository appRepository;

    @Override
    public Long createApp(PayAppSaveReqDTO createReqDTO) {
        validateAppKeyUnique(null, createReqDTO.getAppKey());
        PayAppPO app = PayAppMapper.INSTANCE.toPO(createReqDTO);
        appRepository.save(app);
        return app.getId();
    }

    @Override
    public void updateApp(PayAppSaveReqDTO updateReqDTO) {
        PayAppPO existing = appRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(APP_NOT_FOUND));
        validateAppKeyUnique(updateReqDTO.getId(), updateReqDTO.getAppKey());
        PayAppMapper.INSTANCE.updatePO(updateReqDTO, existing);
        appRepository.save(existing);
    }

    @Override
    public void deleteApp(Long id) {
        appRepository.findById(id).orElseThrow(() -> exception(APP_NOT_FOUND));
        appRepository.deleteById(id);
    }

    @Override
    public PayAppPO getApp(Long id) {
        return appRepository.findById(id).orElseThrow(() -> exception(APP_NOT_FOUND));
    }

    @Override
    public PageResult<PayAppPO> getAppPage(PayAppPageReqDTO pageReqDTO) {
        Specification<PayAppPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, PayAppPO_.name, pageReqDTO.getName());
            eqIfPresent(predicates, cb, root, PayAppPO_.status, CommonStatusEnum.of(pageReqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageUtils.toPageable(pageReqDTO);
        Page<PayAppPO> page = appRepository.findAll(spec, pageable);
        return PageUtils.toPageResult(page);
    }

    @Override
    public PayAppPO validApp(Long id) {
        PayAppPO app = appRepository.findById(id).orElseThrow(() -> exception(APP_NOT_FOUND));
        validateAppEnabled(app);
        return app;
    }

    @Override
    public PayAppPO validApp(String appKey) {
        PayAppPO app = appRepository.findByAppKey(appKey).orElseThrow(() -> exception(APP_NOT_FOUND));
        validateAppEnabled(app);
        return app;
    }

    // ================ helpers ================

    private void validateAppEnabled(PayAppPO app) {
        if (CommonStatusEnum.DISABLE.equals(app.getStatus())) {
            throw exception(APP_NOT_FOUND);
        }
    }

    private void validateAppKeyUnique(Long id, String appKey) {
        Optional<PayAppPO> app = appRepository.findByAppKey(appKey);
        if (app.isEmpty()) {
            return;
        }
        if (id == null || !app.get().getId().equals(id)) {
            throw exception(APP_KEY_DUPLICATE);
        }
    }

}
