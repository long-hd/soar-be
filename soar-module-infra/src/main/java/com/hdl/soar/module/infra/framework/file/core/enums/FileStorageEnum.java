package com.hdl.soar.module.infra.framework.file.core.enums;


import cn.hutool.core.util.ArrayUtil;
import com.hdl.soar.module.infra.framework.file.core.client.FileClient;
import com.hdl.soar.module.infra.framework.file.core.client.FileClientConfig;
import com.hdl.soar.module.infra.framework.file.core.client.db.DBFileClient;
import com.hdl.soar.module.infra.framework.file.core.client.db.DBFileClientConfig;
import com.hdl.soar.module.infra.framework.file.core.client.local.LocalFileClient;
import com.hdl.soar.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import com.hdl.soar.module.infra.framework.file.core.client.s3.S3FileClient;
import com.hdl.soar.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * File storage type enum. Maps a storage code to its config + client classes.
 *
 * <p>Numeric codes kept aligned with the reference for schema/seed compatibility.
 */
@Getter
@AllArgsConstructor
public enum FileStorageEnum {

    DB(1, DBFileClientConfig.class, DBFileClient.class),
    LOCAL(10, LocalFileClientConfig.class, LocalFileClient.class),
    S3(20, S3FileClientConfig.class, S3FileClient.class);

    /**
     * Storage code (persisted in {@code infra_file_config.storage}).
     */
    private final Integer storage;
    /**
     * Config class for this storage type.
     */
    private final Class<? extends FileClientConfig> configClass;
    /**
     * Client class for this storage type.
     */
    private final Class<? extends FileClient> clientClass;

    public static FileStorageEnum getByStorage(Integer storage) {
        return ArrayUtil.firstMatch(o -> o.getStorage().equals(storage), values());
    }

}
