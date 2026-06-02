package com.hdl.soar.framework.common.biz.infra.file;

/**
 * File API for cross-module access. Lets other modules store/read files without depending on infra.
 */
public interface FileCommonApi {

    /**
     * Upload a file via the master storage, with a generated name.
     *
     * @param content file bytes
     * @return access URL
     */
    String createFile(byte[] content);

    /**
     * Upload a file via the master storage.
     *
     * @param name      file name (used for extension / path); may be null
     * @param directory optional sub-directory; may be null
     * @param content   file bytes
     * @return access URL
     */
    String createFile(String name, String directory, byte[] content);

    /**
     * Read a file's content.
     *
     * @param configId storage config id
     * @param path     relative path
     * @return file bytes
     * @throws Exception on read failure
     */
    byte[] getFileContent(Long configId, String path) throws Exception;

}
