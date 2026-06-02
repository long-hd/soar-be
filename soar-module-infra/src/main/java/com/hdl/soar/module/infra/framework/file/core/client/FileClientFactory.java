package com.hdl.soar.module.infra.framework.file.core.client;

import com.hdl.soar.module.infra.framework.file.core.enums.FileStorageEnum;

/**
 * Factory + registry of file clients. Holds live client instances (incl. S3 SDK objects) in memory.
 */
public interface FileClientFactory {

    /**
     * Get a file client by config id.
     *
     * @param configId config id
     * @return the client, or {@code null} if absent
     */
    FileClient getFileClient(Long configId);

    /**
     * Create or refresh a file client for the given config.
     *
     * @param configId config id
     * @param storage  storage code, see {@link FileStorageEnum}
     * @param config   the (typed) config
     */
    <Config extends FileClientConfig> void createOrUpdateFileClient(Long configId, Integer storage, Config config);

}
