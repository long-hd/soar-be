package com.hdl.soar.module.infra.framework.file.core.client.local;

import com.hdl.soar.module.infra.framework.file.core.client.FileClientConfig;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Config for the local-disk file client.
 */
@Data
public class LocalFileClientConfig implements FileClientConfig {

    /**
     * Base directory on disk, e.g. {@code /home/soar/files}.
     */
    @NotEmpty(message = "basePath must not be empty")
    private String basePath;

    /**
     * Custom domain (base URL) used to build the file access URL. Optional.
     */
    @URL(message = "domain must be a valid URL")
    private String domain;

}
