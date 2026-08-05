package com.hdl.soar.module.pay.service.channel;

import cn.hutool.core.lang.Assert;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelSaveReqDTO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO_;
import com.hdl.soar.module.pay.dal.postgres.channel.PayChannelRepository;
import com.hdl.soar.module.pay.enums.PayChannelEnum;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClient;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClientConfig;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClientFactory;
import com.hdl.soar.module.pay.mapper.channel.PayChannelMapper;
import com.hdl.soar.module.pay.service.app.PayAppService;
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
import static com.hdl.soar.module.pay.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayChannelServiceImpl implements PayChannelService {

    PayChannelRepository channelRepository;
    PayAppService appService;
    PayClientFactory payClientFactory;

    @Override
    public Long createChannel(PayChannelSaveReqDTO createReqDTO) {
        validateChannelCode(createReqDTO.getCode());
        appService.getApp(createReqDTO.getAppId()); // assert app exists
        validateChannelUnique(null, createReqDTO.getAppId(), createReqDTO.getCode());

        PayChannelPO channel = PayChannelMapper.INSTANCE.toPO(createReqDTO);
        channelRepository.save(channel);
        return channel.getId();
    }

    @Override
    public void updateChannel(PayChannelSaveReqDTO updateReqDTO) {
        PayChannelPO existing = channelRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(CHANNEL_NOT_FOUND));
        validateChannelCode(updateReqDTO.getCode());
        validateChannelUnique(updateReqDTO.getId(), updateReqDTO.getAppId(), updateReqDTO.getCode());

        PayChannelMapper.INSTANCE.updatePO(updateReqDTO, existing);
        channelRepository.save(existing);
    }

    @Override
    public void deleteChannel(Long id) {
        channelRepository.findById(id)
                .orElseThrow(() -> exception(CHANNEL_NOT_FOUND));
        channelRepository.deleteById(id);
    }

    @Override
    public PayChannelPO getChannel(Long id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> exception(CHANNEL_NOT_FOUND));
    }

    @Override
    public PageResult<PayChannelPO> getChannelPage(PayChannelPageReqDTO pageReqDTO) {
        Specification<PayChannelPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, PayChannelPO_.appId, pageReqDTO.getAppId());
            eqIfPresent(predicates, cb, root, PayChannelPO_.code, pageReqDTO.getCode());
            eqIfPresent(predicates, cb, root, PayChannelPO_.status, CommonStatusEnum.of(pageReqDTO.getStatus()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageUtils.toPageable(pageReqDTO);
        Page<PayChannelPO> page = channelRepository.findAll(spec, pageable);
        return PageUtils.toPageResult(page);
    }

    @Override
    public PayChannelPO validChannel(Long appId, String code) {
        PayChannelPO channel = channelRepository.findByAppIdAndCode(appId, code)
                .orElseThrow(() -> exception(CHANNEL_NOT_FOUND));
        validateChannelEnabled(channel);
        return channel;
    }

    @Override
    public PayChannelPO validChannel(Long id) {
        PayChannelPO channel = channelRepository.findById(id)
                .orElseThrow(() -> exception(CHANNEL_NOT_FOUND));
        validateChannelEnabled(channel);
        return channel;
    }

    // ================ Pay Client ================

    @Override
    public PayClient<?> getPayClient(Long channelId) {
        PayChannelPO channel = validChannel(channelId);
        PayChannelEnum channelEnum = PayChannelEnum.of(channel.getCode());
        Assert.notNull(channelEnum, "Payment channel ({}) is not supported", channel.getCode());
        PayClientConfig config = JsonUtils.parseObject(
                        channel.getConfig(), channelEnum.getConfigClass());
        payClientFactory.createOrUpdatePayClient(channelId, channel.getCode(), config);
        return payClientFactory.getPayClient(channelId);
    }

    // ================ helpers ================

    private void validateChannelEnabled(PayChannelPO channel) {
        if (CommonStatusEnum.DISABLE.equals(channel.getStatus())) {
            throw exception(CHANNEL_NOT_FOUND);
        }
    }

    private void validateChannelCode(String code) {
        if (!PayChannelEnum.exists(code)) {
            throw exception(CHANNEL_CODE_INVALID);
        }
    }

    private void validateChannelUnique(Long id, Long appId, String code) {
        Optional<PayChannelPO> channel = channelRepository.findByAppIdAndCode(appId, code);
        if (channel.isEmpty()) {
            return;
        }
        if (id == null || !channel.get().getId().equals(id)) {
            throw exception(CHANNEL_EXIST_SAME_CODE);
        }
    }

}
