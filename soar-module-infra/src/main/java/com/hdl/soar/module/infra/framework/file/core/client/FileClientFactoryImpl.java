package com.hdl.soar.module.infra.framework.file.core.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.hdl.soar.module.infra.framework.file.core.enums.FileStorageEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default {@link FileClientFactory}. The {@code clients} map is the real client store:
 * it holds live {@code AbstractFileClient} instances (with their S3Client/S3Presigner).
 * In-memory only -- never serialized (e.g. to Redis), because the SDK objects are not serializable.
 */
@Slf4j
public class FileClientFactoryImpl implements FileClientFactory {

    /**
     * client map. key = config id.
     */
    private final ConcurrentMap<Long, AbstractFileClient<?>> clients = new ConcurrentHashMap<>();


    @Override
    public FileClient getFileClient(Long configId) {
        AbstractFileClient<?> client = clients.get(configId);
        if (client == null) {
            log.error("[getFileClient][configId({}) has no client]", configId);
        }
        return client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Config extends FileClientConfig> void createOrUpdateFileClient(Long configId, Integer storage, Config config) {
        AbstractFileClient<Config> client = (AbstractFileClient<Config>) clients.get(configId);
        if (client == null) {
            client = createFileClient(configId, storage, config);
            client.init();
            clients.put(client.getId(), client);
        } else {
            client.refresh(config);
        }
    }

    @SuppressWarnings("unchecked")
    private <Config extends FileClientConfig> AbstractFileClient<Config> createFileClient(
            Long configId, Integer storage, Config config) {
        FileStorageEnum storageEnum = FileStorageEnum.getByStorage(storage);
        Assert.notNull(storageEnum, "file storage type ({}) is invalid", storage);
        return (AbstractFileClient<Config>) ReflectUtil.newInstance(
                storageEnum.getClientClass(), configId, config);
    }

}
