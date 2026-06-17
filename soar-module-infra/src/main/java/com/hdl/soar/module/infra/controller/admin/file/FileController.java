package com.hdl.soar.module.infra.controller.admin.file;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.tenant.core.aop.TenantIgnore;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.*;
import com.hdl.soar.module.infra.dal.entity.file.FilePO;
import com.hdl.soar.module.infra.mapper.file.FileMapper;
import com.hdl.soar.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - File Storage")
@Slf4j
@Validated
@RestController
@RequestMapping("/infra/file")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    private static final Tika TIKA = new Tika();

    FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file", description = "Mode 1: backend upload (works for all storage types)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schemaProperties = {
                            @SchemaProperty(name = "file",
                                    schema = @Schema(type = "string", format = "binary", description = "File")),
                    }))
    @PreAuthorize("@ss.hasPermission('infra:file:create')")
    public CommonResult<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "directory", required = false) String directory) throws Exception {
        byte[] content = IoUtil.readBytes(file.getInputStream());
        return success(fileService.createFile(file.getOriginalFilename(), directory, content));
    }


    @GetMapping("/presigned-url")
    @Operation(summary = "Get a presigned upload URL",
            description = "Mode 2 (step 1): client uploads directly to S3-compatible storage")
    @Parameters({
            @Parameter(name = "name", description = "File name", required = true),
            @Parameter(name = "directory", description = "Sub-directory")
    })
    @PreAuthorize("@ss.hasPermission('infra:file:create')")
    public CommonResult<FilePresignedUrlRespDTO> getFilePresignedUrl(
            @RequestParam("name") String name,
            @RequestParam(value = "directory", required = false) String directory) throws Exception {
        return success(fileService.getFilePresignedUrl(name, directory));
    }

    @PostMapping("/create")
    @Operation(summary = "Record an uploaded file's metadata",
            description = "Mode 2 (step 2): after a presigned direct upload")
    @PreAuthorize("@ss.hasPermission('infra:file:create')")
    public CommonResult<Long> createFile(@Valid @RequestBody FileCreateReqDTO createReqDTO) {
        return success(fileService.createFile(createReqDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "Get file page")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<PageResult<FileRespDTO>> getFilePage(@Valid FilePageReqDTO pageReqDTO) {
        PageResult<FilePO> pageResult = fileService.getFilePage(pageReqDTO);
        return success(new PageResult<>(
                FileMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a file")
    @Parameter(name = "id", description = "File ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) throws Exception {
        fileService.deleteFile(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "Bulk delete files")
    @Parameter(name = "ids", description = "File IDs (comma-separated)", required = true)
    @PreAuthorize("@ss.hasPermission('infra:file:delete')")
    public CommonResult<Boolean> deleteFileList(@RequestParam("ids") List<Long> ids) throws Exception {
        fileService.deleteFileList(ids);
        return success(true);
    }

    @GetMapping("/{configId}/get/**")
    @Operation(summary = "Download a file (public)")
    @Parameter(name = "configId", description = "Storage config ID", required = true)
    @PermitAll
    @TenantIgnore
    public void getFileContent(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable("configId") Long configId) throws Exception {
        // Extract the path after "/get/".
        String path = StrUtil.subAfter(request.getRequestURI(), "/get/", false);
        if (StrUtil.isEmpty(path)) {
            throw new IllegalArgumentException("A trailing path is required after /get/");
        }
        // Decode (handles non-ASCII paths).
        path = URLUtil.decode(path, StandardCharsets.UTF_8, false);

        byte[] content = fileService.getFileContent(configId, path);
        if (content == null) {
            log.warn("[getFileContent][configId({}) path({}) file not found]", configId, path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        writeContent(response, path, content);
    }

    /**
     * Write file bytes to the response. Detects the content type (Tika), so images/PDFs can render
     * inline in the browser; falls back to {@code application/octet-stream} (download) on failure.
     */
    private void writeContent(HttpServletResponse response, String path, byte[] content) throws IOException {
        String contentType;
        try {
            contentType = TIKA.detect(content, path); // path hint improves accuracy (extension)
        } catch (Exception ex) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        response.setContentType(contentType);
        response.setContentLength(content.length);
        // inline so browsers render renderable types; filename from the path tail.
        String filename = StrUtil.subAfter(path, "/", true);
        String disposition = contentType.startsWith("image/") ? "inline" : "attachment";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                disposition + "; filename=\"" + filename + "\"; filename*=UTF-8''"
                        + URLUtil.encode(filename, StandardCharsets.UTF_8));
        response.getOutputStream().write(content);
    }

}
