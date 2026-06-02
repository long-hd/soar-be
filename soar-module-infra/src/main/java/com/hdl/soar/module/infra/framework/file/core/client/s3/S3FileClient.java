package com.hdl.soar.module.infra.framework.file.core.client.s3;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.hdl.soar.module.infra.framework.file.core.client.AbstractFileClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

/**
 * S3-protocol file client. Works with SeaweedFS and AWS S3.
 */
public class S3FileClient extends AbstractFileClient<S3FileClientConfig> {

    private static final Duration EXPIRATION_DEFAULT = Duration.ofHours(24);
    private static final String DEFAULT_REGION = "us-east-1";

    private S3Client client;
    private S3Presigner presigner;

    public S3FileClient(Long id, S3FileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // Fill domain if missing.
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        Region region = Region.of(resolveRegion());
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.getAccessKey(), config.getAccessSecret()));
        URI endpoint = URI.create(buildEndpoint());
        URI presignerEndpoint = URI.create(buildPresignerEndpoint());
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(Boolean.TRUE.equals(config.getEnablePathStyleAccess()))
                .chunkedEncodingEnabled(false) // required for SeaweedFS/MinIO compatibility
                .build();
        client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .endpointOverride(endpoint)
                .serviceConfiguration(serviceConfiguration)
                .build();
        presigner = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .endpointOverride(presignerEndpoint)
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .contentType(type)
                .contentLength((long) content.length)
                .build();
        client.putObject(putRequest, RequestBody.fromBytes(content));
        return presignGetUrl(path, null);
    }

    @Override
    public void delete(String path) throws Exception {
        client.deleteObject(DeleteObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build());
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build();
        return IoUtil.readBytes(client.getObject(getRequest));
    }

    @Override
    public String presignPutUrl(String path) {
        return presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(EXPIRATION_DEFAULT)
                        .putObjectRequest(b -> b.bucket(config.getBucket()).key(path)).build())
                .url().toString();
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        // Convert the full url back to a path (strip domain prefix + query).
        String path = StrUtil.removePrefix(url, config.getDomain() + "/");
        path = URLUtil.decode(StrUtil.subBefore(path, "?", false));

        // Public access: no signing.
        if (!BooleanUtil.isFalse(config.getEnablePublicAccess())) {
            return config.getDomain() + "/" + path;
        }

        // Private access: presigned GET URL.
        String finalPath = path;
        Duration expiration = expirationSeconds != null ? Duration.ofSeconds(expirationSeconds) : EXPIRATION_DEFAULT;
        URL signedUrl = presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(b -> b.bucket(config.getBucket()).key(finalPath)).build())
                .url();
        return signedUrl.toString();
    }

    /**
     * Build the access domain from endpoint + bucket.
     */
    private String buildDomain() {
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            // e.g. SeaweedFS/MinIO: http://host:port/bucket
            return StrUtil.format("{}/{}", config.getEndpoint(), config.getBucket());
        }
        // Virtual-host style: https://bucket.endpoint
        return StrUtil.format("https://{}.{}", config.getBucket(), config.getEndpoint());
    }

    /**
     * Prepend protocol to the endpoint if missing.
     */
    private String buildEndpoint() {
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return config.getEndpoint();
        }
        return StrUtil.format("https://{}", config.getEndpoint());
    }

    /**
     * Build the presigner endpoint (domain without the bucket segment).
     */
    private String buildPresignerEndpoint() {
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        if (Boolean.TRUE.equals(config.getEnablePathStyleAccess())) {
            return StrUtil.removeSuffix(config.getDomain(), StrUtil.format("/{}", config.getBucket()));
        }
        return StrUtil.replace(config.getDomain(), StrUtil.format("://{}.", config.getBucket()), "://");
    }

    /**
     * Resolve AWS region. Priority: configured region &gt; parsed from AWS endpoint &gt; default us-east-1.
     * (China-cloud endpoint parsing removed — SeaweedFS + AWS only.)
     */
    private String resolveRegion() {
        if (StrUtil.isNotEmpty(config.getRegion())) {
            return config.getRegion();
        }
        String endpoint = config.getEndpoint();
        if (StrUtil.isEmpty(endpoint)) {
            return DEFAULT_REGION;
        }
        String host = endpoint;
        if (HttpUtil.isHttp(endpoint) || HttpUtil.isHttps(endpoint)) {
            try {
                host = URI.create(endpoint).getHost();
            } catch (Exception e) {
                return DEFAULT_REGION;
            }
        }
        if (StrUtil.isEmpty(host)) {
            return DEFAULT_REGION;
        }
        // AWS S3 format: s3.{region}.amazonaws.com
        if (host.contains("amazonaws.com") && host.startsWith("s3.")) {
            String regionPart = host.substring(3, host.indexOf(".amazonaws.com"));
            if (StrUtil.isNotEmpty(regionPart) && !regionPart.equals("accelerate")) {
                return regionPart;
            }
        }
        return DEFAULT_REGION;
    }

}
