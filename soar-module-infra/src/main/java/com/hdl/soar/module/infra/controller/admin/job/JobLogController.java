package com.hdl.soar.module.infra.controller.admin.job;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.infra.controller.admin.job.dto.log.JobLogPageReqDTO;
import com.hdl.soar.module.infra.controller.admin.job.dto.log.JobLogRespDTO;
import com.hdl.soar.module.infra.dal.entity.job.JobLogPO;
import com.hdl.soar.module.infra.mapper.job.JobLogMapper;
import com.hdl.soar.module.infra.service.job.JobLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Job Log")
@Validated
@RestController
@RequestMapping("/infra/job-log")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobLogController {

    JobLogService jobLogService;

    @GetMapping("/get")
    @Operation(summary = "Get job log")
    @Parameter(name = "id", description = "Log ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<JobLogRespDTO> getJobLog(@RequestParam("id") Long id) {
        JobLogPO log = jobLogService.getJobLog(id);
        return success(JobLogMapper.INSTANCE.toDTO(log));
    }

    @GetMapping("/page")
    @Operation(summary = "Get job log page")
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<PageResult<JobLogRespDTO>> getJobLogPage(@Valid JobLogPageReqDTO pageReqDTO) {
        PageResult<JobLogPO> pageResult = jobLogService.getJobLogPage(pageReqDTO);
        return success(new PageResult<>(JobLogMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()));
    }

}
