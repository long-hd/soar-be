package com.hdl.soar.module.infra.framework.file.core.client.local;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.hdl.soar.module.infra.framework.file.core.client.AbstractFileClient;

import java.io.File;

/**
 * Local-disk file client.
 */
public class LocalFileClient extends AbstractFileClient<LocalFileClientConfig> {

    public LocalFileClient(Long id, LocalFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {

    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        FileUtil.writeBytes(content, getFilePath(path));
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) throws Exception {
        FileUtil.del(getFilePath(path));
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        try {
            return FileUtil.readBytes(getFilePath(path));
        } catch (IORuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("File not exist:")) {
                return null;
            }
            throw ex;
        }
    }

    private String getFilePath(String path) {
        return config.getBasePath() + File.separator + path;
    }

}
