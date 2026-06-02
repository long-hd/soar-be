package com.hdl.soar.module.infra.framework.file.core.client.db;

import com.hdl.soar.module.infra.framework.file.core.client.FileClientConfig;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Config for the DB-backed file client.
 */
@Data
public class DBFileClientConfig implements FileClientConfig {

    /**
     * Custom domain (base URL) used to build the file access URL. Optional.
     */
    @URL(message = "domain must be a valid URL")
    private String domain;

}
