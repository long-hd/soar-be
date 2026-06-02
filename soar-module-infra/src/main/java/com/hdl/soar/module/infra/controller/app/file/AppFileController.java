package com.hdl.soar.module.infra.controller.app.file;

import cn.hutool.core.io.IoUtil;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "User App - File Storage")
@Validated
@RestController
@RequestMapping("/infra/file")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppFileController {

    FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file (app)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schemaProperties = {
                            @SchemaProperty(name = "file",
                                    schema = @Schema(type = "string", format = "binary", description = "File")),
                    }))
    @PermitAll
    public CommonResult<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "directory", required = false) String directory) throws Exception {
        byte[] content = IoUtil.readBytes(file.getInputStream());
        return success(fileService.createFile(file.getOriginalFilename(), directory, content));
    }

}