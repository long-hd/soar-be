package com.hdl.soar.module.pay.controller.admin.notify;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.controller.admin.notify.dto.PayNotifyLogRespDTO;
import com.hdl.soar.module.pay.controller.admin.notify.dto.PayNotifyTaskPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.notify.dto.PayNotifyTaskRespDTO;
import com.hdl.soar.module.pay.dal.entity.notify.PayNotifyTaskPO;
import com.hdl.soar.module.pay.mapper.notify.PayNotifyMapper;
import com.hdl.soar.module.pay.service.notify.PayNotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

/**
 * Read-only visibility into the notify outbox. Base path {@code /pay/notify-task} to avoid colliding
 * with the channel-callback controller at {@code /pay/notify}. Resend is deferred to a later slice.
 */
@Tag(name = "Admin Backend - Payment Notify Task")
@RestController
@RequestMapping("/pay/notify-task")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayNotifyTaskController {

    PayNotifyService notifyService;

    @GetMapping("/get")
    @Operation(summary = "Get notify task")
    @Parameter(name = "id", description = "Task ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:notify:query')")
    public CommonResult<PayNotifyTaskRespDTO> getNotifyTask(@RequestParam("id") Long id) {
        PayNotifyTaskPO task = notifyService.getNotifyTask(id);
        return success(PayNotifyMapper.INSTANCE.toDTO(task));
    }

    @GetMapping("/page")
    @Operation(summary = "Get notify task paginated list")
    @PreAuthorize("@ss.hasPermission('pay:notify:query')")
    public CommonResult<PageResult<PayNotifyTaskRespDTO>> getNotifyTaskPage(@Valid PayNotifyTaskPageReqDTO pageReqDTO) {
        PageResult<PayNotifyTaskPO> pageResult = notifyService.getNotifyTaskPage(pageReqDTO);
        return success(new PageResult<>(
                PayNotifyMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/log-list")
    @Operation(summary = "List delivery attempts for a task")
    @Parameter(name = "taskId", description = "Task ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:notify:query')")
    public CommonResult<java.util.List<PayNotifyLogRespDTO>> getNotifyLogList(@RequestParam("taskId") Long taskId) {
        return success(PayNotifyMapper.INSTANCE.toLogDTOList(notifyService.getNotifyLogList(taskId)));
    }

}
