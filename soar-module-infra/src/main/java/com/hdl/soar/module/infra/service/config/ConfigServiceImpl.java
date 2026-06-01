package com.hdl.soar.module.infra.service.config;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.config.dto.ConfigSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.config.ConfigPO;
import com.hdl.soar.module.infra.dal.entity.config.ConfigPO_;
import com.hdl.soar.module.infra.dal.postgres.config.ConfigRepository;
import com.hdl.soar.module.infra.enums.config.ConfigTypeEnum;
import com.hdl.soar.module.infra.mapper.config.ConfigMapper;
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

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;
import static com.hdl.soar.module.infra.enums.ErrorCodeConstants.*;

@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigServiceImpl implements ConfigService {

    ConfigRepository configRepository;

    @Override
    public Long createConfig(ConfigSaveReqDTO createReqDTO) {
        // Validate key uniqueness
        validateConfigKeyUnique(null, createReqDTO.getKey());
        // Create
        ConfigPO config = ConfigMapper.INSTANCE.toPO(createReqDTO);
        config.setType(ConfigTypeEnum.CUSTOM);
        configRepository.save(config);
        return config.getId();
    }

    @Override
    public void updateConfig(ConfigSaveReqDTO updateReqDTO) {
        // Validate exists
        ConfigPO config = configRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(CONFIG_NOT_EXISTS));
        // Validate key uniqueness
        validateConfigKeyUnique(updateReqDTO.getId(), updateReqDTO.getKey());
        // Update
        ConfigMapper.INSTANCE.updatePO(updateReqDTO, config);
        configRepository.save(config);
    }

    @Override
    public void deleteConfig(Long id) {
        // Validate exists
        ConfigPO config = configRepository.findById(id)
                .orElseThrow(() -> exception(CONFIG_NOT_EXISTS));
        // SYSTEM type cannot be deleted
        if (ConfigTypeEnum.SYSTEM.equals(config.getType())) {
            throw exception(CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE);
        }
        configRepository.deleteById(id);
    }

    @Override
    public ConfigPO getConfig(Long id) {
        return configRepository.findById(id).orElse(null);
    }

    @Override
    public ConfigPO getConfigByKey(String key) {
        return configRepository.findByConfigKey(key).orElse(null);
    }

    @Override
    public PageResult<ConfigPO> getConfigPage(ConfigPageReqDTO pageReqDTO) {
        Specification<ConfigPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, ConfigPO_.name, pageReqDTO.getName());
            likeIfPresent(predicates, cb, root, ConfigPO_.configKey, pageReqDTO.getKey());
            eqIfPresent(predicates, cb, root, ConfigPO_.type,
                    ConfigTypeEnum.of(pageReqDTO.getType()));
            betweenIfPresent(predicates, cb, root, ConfigPO_.createTime, pageReqDTO.getCreateTime());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ConfigPO> page = configRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, ConfigPO_.ID)));
        return PageUtils.toPageResult(page);
    }

    // ========== Validation ==========

    private void validateConfigKeyUnique(Long id, String key) {
        ConfigPO config = configRepository.findByConfigKey(key).orElse(null);
        if (config == null) {
            return;
        }
        if (id == null || !config.getId().equals(id)) {
            throw exception(CONFIG_KEY_DUPLICATE);
        }
    }

}
