package com.hdl.soar.module.infra.service.file;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.FileCreateReqDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.FilePageReqDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.FilePresignedUrlRespDTO;
import com.hdl.soar.module.infra.dal.entity.file.FilePO;

import java.util.List;

public interface FileService {

    /**
     * Mode 1: backend upload. Stores bytes via the master client and records metadata.
     *
     * @return access URL
     */
    String createFile(String name, String directory, byte[] content);

    /**
     * Mode 2 (sign): generate a presigned upload URL via the master client (S3 only).
     */
    FilePresignedUrlRespDTO getFilePresignedUrl(String name, String directory) throws Exception;

    /**
     * Mode 2 (record): persist metadata after a direct (presigned) upload.
     *
     * @return file id
     */
    Long createFile(FileCreateReqDTO createReqDTO);

    void deleteFile(Long id) throws Exception;

    void deleteFileList(List<Long> ids);

    /**
     * Read content for the public download endpoint.
     */
    byte[] getFileContent(Long configId, String path) throws Exception;

    PageResult<FilePO> getFilePage(FilePageReqDTO pageReqDTO);

}
