package com.hdl.soar.module.infra.framework.file.core.client.db;

import cn.hutool.extra.spring.SpringUtil;
import com.hdl.soar.module.infra.dal.entity.file.FileContentPO;
import com.hdl.soar.module.infra.dal.postgres.file.FileContentRepository;
import com.hdl.soar.module.infra.framework.file.core.client.AbstractFileClient;

import java.util.Optional;

/**
 * DB-backed file client. Stores bytes in {@code infra_file_content}.
 */
public class DBFileClient extends AbstractFileClient<DBFileClientConfig> {

    private FileContentRepository fileContentRepository;

    public DBFileClient(Long id, DBFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // Clients are created reflectively (not Spring beans), so resolve the repository from the context.
        fileContentRepository = SpringUtil.getBean(FileContentRepository.class);
    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        FileContentPO contentPO = FileContentPO.builder()
                .configId(getId())
                .path(path)
                .content(content)
                .build();
        fileContentRepository.save(contentPO);
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) throws Exception {
        // Soft delete (ADR-008): load then delete through the soft-delete repository.
        fileContentRepository.findByConfigIdAndPath(getId(), path)
                .forEach(fileContentRepository::delete);
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        // Latest upload wins (highest id).
        Optional<FileContentPO> po = fileContentRepository.findFirstByConfigIdAndPathOrderByIdDesc(getId(), path);
        return po.map(FileContentPO::getContent).orElse(null);
    }

}
