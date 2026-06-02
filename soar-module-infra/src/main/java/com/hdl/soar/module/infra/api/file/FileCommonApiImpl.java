package com.hdl.soar.module.infra.api.file;

import com.hdl.soar.framework.common.biz.infra.file.FileCommonApi;
import com.hdl.soar.module.infra.service.file.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCommonApiImpl implements FileCommonApi {

    FileService fileService;

    @Override
    public String createFile(byte[] content) {
        return createFile(null, null, content);
    }

    @Override
    public String createFile(String name, String directory, byte[] content) {
        return fileService.createFile(name, directory, content);
    }

    @Override
    public byte[] getFileContent(Long configId, String path) throws Exception {
        return fileService.getFileContent(configId, path);
    }

}