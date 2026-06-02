package com.hdl.soar.module.infra.framework.file.core.client.s3;

import com.hdl.soar.module.infra.framework.file.core.client.FileClientConfig;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Config for the S3-compatible file client (SeaweedFS, AWS S3).
 */
@Data
public class S3FileClientConfig implements FileClientConfig {

    /**
     * Endpoint, e.g. {@code http://127.0.0.1:8333} (SeaweedFS) or {@code s3.us-east-1.amazonaws.com} (AWS).
     */
    @NotNull(message = "endpoint must not be null")
    private String endpoint;

    /**
     * Custom access domain. Auto-derived from endpoint + bucket when empty.
     */
    @URL(message = "domain must be a valid URL")
    private String domain;

    /**
     * Bucket name.
     */
    @NotNull(message = "bucket must not be null")
    private String bucket;

    /**
     * Access key.
     */
    @NotNull(message = "accessKey must not be null")
    private String accessKey;

    /**
     * Access secret.
     */
    @NotNull(message = "accessSecret must not be null")
    private String accessSecret;

    /**
     * Path-style access. {@code true} for SeaweedFS / MinIO (mandatory); usually {@code false} for AWS.
     */
    @NotNull(message = "enablePathStyleAccess must not be null")
    private Boolean enablePathStyleAccess;

    /**
     * Public access. {@code true} = return public URL; {@code false} = return presigned GET URL.
     */
    @NotNull(message = "enablePublicAccess must not be null")
    private Boolean enablePublicAccess;

    /**
     * AWS region. Optional; defaults to {@code us-east-1} (SeaweedFS ignores it but the SDK requires one).
     */
    private String region;

}
