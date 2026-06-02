package com.hdl.soar.module.infra.framework.file.core.client;

/**
 * File client. Abstracts a storage backend (DB, local disk, S3-compatible).
 */
public interface FileClient {

    /**
     * @return the client (config) id
     */
    Long getId();

    /**
     * Upload a file.
     *
     * @param content file bytes
     * @param path    relative path (key)
     * @param type    content type (MIME)
     * @return full HTTP-accessible URL
     * @throws Exception on upload failure
     */
    String upload(byte[] content, String path, String type) throws Exception;

    /**
     * Delete a file.
     *
     * @param path relative path (key)
     * @throws Exception on delete failure
     */
    void delete(String path) throws Exception;

    /**
     * Read a file's content.
     *
     * @param path relative path (key)
     * @return file bytes, or {@code null} if not found
     * @throws Exception on read failure
     */
    byte[] getContent(String path) throws Exception;

    // ========== Presign (S3 only) ==========

    /**
     * Presigned URL for direct upload (PUT). Only S3 supports this.
     *
     * @param path relative path (key)
     * @return presigned upload URL
     */
    default String presignPutUrl(String path) {
        throw new UnsupportedOperationException("presign is not supported by this storage");
    }

    /**
     * Presigned URL for read (GET), or a public URL when public access is enabled.
     *
     * @param url               full file access URL (or path)
     * @param expirationSeconds validity in seconds; {@code null} = default
     * @return presigned (or public) read URL
     */
    default String presignGetUrl(String url, Integer expirationSeconds) {
        throw new UnsupportedOperationException("presign is not supported by this storage");
    }

}