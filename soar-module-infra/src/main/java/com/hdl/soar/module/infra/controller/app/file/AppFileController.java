package com.hdl.soar.module.infra.controller.app.file;

import cn.hutool.core.io.IoUtil;
import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.module.infra.controller.admin.file.dto.file.FileUploadReqDTO;
import com.hdl.soar.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PermitAll
    public CommonResult<String> uploadFile(@Valid FileUploadReqDTO uploadReqDTO) throws Exception {
        var file = uploadReqDTO.getFile();
        byte[] content = IoUtil.readBytes(file.getInputStream());
        return success(fileService.createFile(file.getOriginalFilename(), uploadReqDTO.getDirectory(), content));
    }

}