package com.hdl.soar.module.infra.service.file;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.FileCreateReqDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.FilePageReqDTO;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.FilePresignedUrlRespDTO;
import com.hdl.soar.module.infra.dal.entity.file.FilePO;
import com.hdl.soar.module.infra.dal.entity.file.FilePO_;
import com.hdl.soar.module.infra.dal.postgres.file.FileRepository;
import com.hdl.soar.module.infra.framework.file.core.client.FileClient;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;
import static com.hdl.soar.module.infra.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {

    private static final Tika TIKA = new Tika();

    FileRepository fileRepository;
    FileConfigService fileConfigService;

    @Override
    public String createFile(String name, String directory, byte[] content) {
        if (content == null || content.length == 0) {
            throw exception(FILE_IS_EMPTY);
        }
        // Resolve type + name + path.
        String type = detectType(content);
        if (StrUtil.isEmpty(name)) {
            name = IdUtil.fastSimpleUUID();
        }
        String path = generateUploadPath(name, directory);
        // Upload via master client.
        FileClient client = fileConfigService.getMasterFileClient();
        if (client == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }
        String url;
        try {
            url = client.upload(content, path, type);
        } catch (Exception ex) {
            throw new RuntimeException("File upload failed: " + ex.getMessage(), ex);
        }
        // Record metadata.
        FilePO file = FilePO.builder()
                .configId(client.getId())
                .name(name)
                .path(path)
                .url(url)
                .type(type)
                .size(content.length)
                .build();
        fileRepository.save(file);
        return url;
    }

    @Override
    public FilePresignedUrlRespDTO getFilePresignedUrl(String name, String directory) throws Exception {
        FileClient client = fileConfigService.getMasterFileClient();
        if (client == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }
        String path = generateUploadPath(StrUtil.isEmpty(name) ? IdUtil.fastSimpleUUID() : name, directory);
        try {
            String uploadUrl = client.presignPutUrl(path); // unsupported for DB/LOCAL
            String url = client.presignGetUrl(path, null);
            FilePresignedUrlRespDTO resp = new FilePresignedUrlRespDTO();
            resp.setConfigId(client.getId());
            resp.setPath(path);
            resp.setUploadUrl(uploadUrl);
            resp.setUrl(url);
            return resp;
        } catch (UnsupportedOperationException ex) {
            throw exception(FILE_PRESIGN_NOT_SUPPORTED);
        }
    }

    @Override
    public Long createFile(FileCreateReqDTO createReqDTO) {
        FilePO file = FilePO.builder()
                .configId(createReqDTO.getConfigId())
                .name(createReqDTO.getName())
                .path(createReqDTO.getPath())
                .url(createReqDTO.getUrl())
                .type(createReqDTO.getType())
                .size(ObjUtil.defaultIfNull(createReqDTO.getSize(), 0))
                .build();
        fileRepository.save(file);
        return file.getId();
    }

    @Override
    public void deleteFile(Long id) throws Exception {
        FilePO file = fileRepository.findById(id).orElseThrow(() -> exception(FILE_NOT_EXISTS));
        FileClient client = fileConfigService.getFileClient(file.getConfigId());
        if (client != null) {
            client.delete(file.getPath());
        }
        fileRepository.deleteById(id);
    }

    @Override
    public byte[] getFileContent(Long configId, String path) throws Exception {
        FileClient client = fileConfigService.getFileClient(configId);
        if (client == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }
        return client.getContent(path);
    }

    @Override
    public PageResult<FilePO> getFilePage(FilePageReqDTO pageReqDTO) {
        Specification<FilePO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            likeIfPresent(predicates, cb, root, FilePO_.name, pageReqDTO.getName());
            likeIfPresent(predicates, cb, root, FilePO_.path, pageReqDTO.getPath());
            likeIfPresent(predicates, cb, root, FilePO_.type, pageReqDTO.getType());
            betweenIfPresent(predicates, cb, root, FilePO_.createTime, pageReqDTO.getCreateTime());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<FilePO> page = fileRepository.findAll(spec,
                PageUtils.toPageable(pageReqDTO, Sort.by(Sort.Direction.DESC, FilePO_.ID)));
        return PageUtils.toPageResult(page);
    }

    // ========== Helpers ==========

    /**
     * Detect MIME type from content (Tika). Falls back to a generic type on failure.
     */
    private String detectType(byte[] content) {
        try {
            return TIKA.detect(content);
        } catch (Exception ex) {
            return "application/octet-stream";
        }
    }

    /**
     * Build the storage path: {@code [directory/]yyyyMMdd/<name>}.
     */
    private String generateUploadPath(String name, String directory) {
        String datePrefix = LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN); // yyyyMMdd
        String path = datePrefix + "/" + name;
        if (StrUtil.isNotEmpty(directory)) {
            path = directory + "/" + path;
        }
        return path;
    }

}
