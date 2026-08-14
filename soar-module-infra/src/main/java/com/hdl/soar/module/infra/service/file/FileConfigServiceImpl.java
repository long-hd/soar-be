package com.hdl.soar.module.infra.service.file;

import cn.hutool.core.collection.CollUtil;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.cache.CacheUtils;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.framework.common.util.validation.ValidationUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.file.FileConfigPO;
import com.hdl.soar.module.infra.dal.entity.file.FileConfigPO_;
import com.hdl.soar.module.infra.dal.postgres.file.FileConfigRepository;
import com.hdl.soar.module.infra.framework.file.core.client.FileClient;
import com.hdl.soar.module.infra.framework.file.core.client.FileClientConfig;
import com.hdl.soar.module.infra.framework.file.core.client.FileClientFactory;
import com.hdl.soar.module.infra.framework.file.core.enums.FileStorageEnum;
import com.hdl.soar.module.infra.mapper.file.FileConfigMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;
import static com.hdl.soar.module.infra.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileConfigServiceImpl implements FileConfigService {

    /**
     * Cache key for the master client (configs use their real id; 0 is reserved for
     * "master").
     */
    private static final Long CACHE_MASTER_ID = 0L;

    FileConfigRepository fileConfigRepository;
    FileClientFactory fileClientFactory;
    Validator validator;

    /**
     * Client cache. key = config id (or {@link #CACHE_MASTER_ID} for master).
     * Refreshed every 10s so external config changes are picked up without restart.
     * Built in {@link #init()} because the Caffeine builder needs the loader.
     */
    @NonFinal
    LoadingCache<Long, FileClient> clientCache;

    @PostConstruct
    public void init() {
        // Build the Caffeine cache. The loader (re)creates the client via the factory.
        this.clientCache = CacheUtils.buildAsyncReloadingCaffeine(Duration.ofSeconds(10), id -> {
            FileConfigPO config = CACHE_MASTER_ID.equals(id)
                    ? fileConfigRepository.findByMasterTrue().orElse(null)
                    : fileConfigRepository.findById(id).orElse(null);
            if (config == null) {
                return null;
            }
            FileClientConfig clientConfig = parseClientConfig(config.getStorage(), config.getConfig());
            fileClientFactory.createOrUpdateFileClient(config.getId(), config.getStorage(), clientConfig);
            return fileClientFactory.getFileClient(config.getId());
        });
    }

    @Override
    public Long createFileConfig(FileConfigSaveReqDTO createReqDTO) {
        FileClientConfig clientConfig = parseClientConfig(createReqDTO.getStorage(), createReqDTO.getConfig());
        FileConfigPO config = FileConfigMapper.INSTANCE.toPO(createReqDTO);
        config.setConfig(JsonUtils.toJsonString(clientConfig));
        config.setMaster(false); // never master on create; use update-master explicitly
        fileConfigRepository.save(config);
        return config.getId();
    }

    @Override
    public void updateFileConfig(FileConfigSaveReqDTO updateReqDTO) {
        FileConfigPO config = fileConfigRepository.findById(updateReqDTO.getId())
                .orElseThrow(() -> exception(FILE_CONFIG_NOT_EXISTS));
        FileClientConfig clientConfig = parseClientConfig(updateReqDTO.getStorage(), updateReqDTO.getConfig());
        FileConfigMapper.INSTANCE.updatePO(updateReqDTO, config);
        config.setConfig(JsonUtils.toJsonString(clientConfig));
        fileConfigRepository.save(config);
        clearCache();
    }

    @Override
    public void updateFileConfigMaster(Long id) {
        FileConfigPO config = fileConfigRepository.findById(id)
                .orElseThrow(() -> exception(FILE_CONFIG_NOT_EXISTS));
        // Demote existing masters.
        List<FileConfigPO> oldMasters = fileConfigRepository.findByMasterTrueAndIdNot(id);
        oldMasters.forEach(m -> m.setMaster(false));
        fileConfigRepository.saveAll(oldMasters);
        // Promote this one.
        config.setMaster(true);
        fileConfigRepository.save(config);
        clearCache();
    }

    @Override
    public void deleteFileConfig(Long id) {
        FileConfigPO config = fileConfigRepository.findById(id)
                .orElseThrow(() -> exception(FILE_CONFIG_NOT_EXISTS));
        if (Boolean.TRUE.equals(config.getMaster())) {
            throw exception(FILE_CONFIG_DELETE_FAIL_MASTER);
        }
        fileConfigRepository.deleteById(id);
        clearCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileConfigList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids))
            return;

        // Master guard — fetch and check before any delete
        List<FileConfigPO> configs = fileConfigRepository.findAllById(ids);
        if (configs.size() != ids.size()) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }
        boolean anyMaster = configs.stream().anyMatch(FileConfigPO::getMaster);
        if (anyMaster) {
            throw exception(FILE_CONFIG_DELETE_FAIL_MASTER);
        }

        // Cache invalidation parity với single deleteFileConfig
        fileConfigRepository.deleteAllById(ids);
        clearCache();
    }

    @Override
    public FileConfigPO getFileConfig(Long id) {
        return fileConfigRepository.findById(id)
                .orElseThrow(() -> exception(FILE_CONFIG_NOT_EXISTS));
    }

    @Override
    public PageResult<FileConfigPO> getFileConfigPage(FileConfigPageReqDTO pageReqDTO) {
        Specification<FileConfigPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, FileConfigPO_.name, pageReqDTO.getName());
            eqIfPresent(predicates, cb, root, FileConfigPO_.storage, pageReqDTO.getStorage());
            betweenIfPresent(predicates, cb, root, FileConfigPO_.createTime, pageReqDTO.getCreateTime());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<FileConfigPO> page = fileConfigRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, FileConfigPO_.ID)));
        return PageUtils.toPageResult(page);
    }

    @Override
    public String testFileConfig(Long id) throws Exception {
        FileClient client = getFileClient(id);
        if (client == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }
        byte[] content = "Soar file storage test.".getBytes();
        return client.upload(content, "test.txt", "text/plain");
    }

    @Override
    public FileClient getFileClient(Long id) {
        return clientCache.get(id);
    }

    @Override
    public FileClient getMasterFileClient() {
        return clientCache.get(CACHE_MASTER_ID);
    }

    // ========== Helpers ==========

    /**
     * Resolve and validate the typed client config from a storage code + raw config
     * (map or JSON string).
     * Core of decision (b): the type comes from {@code storage}, not from any
     * {@code @class} in the JSON.
     */
    private FileClientConfig parseClientConfig(Integer storage, Object rawConfig) {
        FileStorageEnum storageEnum = FileStorageEnum.getByStorage(storage);
        if (storageEnum == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS); // invalid storage type
        }
        String json = rawConfig instanceof String s ? s : JsonUtils.toJsonString(rawConfig);
        FileClientConfig config = (FileClientConfig) JsonUtils.parseObject2(json, storageEnum.getConfigClass());
        // Bean validation against the concrete config type.
        ValidationUtils.validate(validator, config);
        return config;
    }

    /**
     * Invalidate the client cache so the next access reloads from DB.
     */
    private void clearCache() {
        clientCache.invalidateAll();
    }

}
