package com.hdl.soar.module.infra.service.file;

import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.config.FileConfigSaveReqDTO;
import com.hdl.soar.module.infra.dal.entity.file.FileConfigPO;
import com.hdl.soar.module.infra.framework.file.core.client.FileClient;

public interface FileConfigService {

    Long createFileConfig(FileConfigSaveReqDTO createReqDTO);

    void updateFileConfig(FileConfigSaveReqDTO updateReqDTO);

    void updateFileConfigMaster(Long id);

    void deleteFileConfig(Long id);

    FileConfigPO getFileConfig(Long id);

    PageResult<FileConfigPO> getFileConfigPage(FileConfigPageReqDTO pageReqDTO);

    /**
     * Test a config by uploading a small sample file.
     *
     * @param id config id
     * @return access URL of the test file
     */
    String testFileConfig(Long id) throws Exception;

    /**
     * Get the file client for a specific config.
     */
    FileClient getFileClient(Long id);

    /**
     * Get the master file client.
     */
    FileClient getMasterFileClient();

}