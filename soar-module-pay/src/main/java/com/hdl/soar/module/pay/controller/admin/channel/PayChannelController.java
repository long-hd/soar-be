package com.hdl.soar.module.pay.controller.admin.channel;

import com.hdl.soar.framework.common.pojo.CommonResult;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelPageReqDTO;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelRespDTO;
import com.hdl.soar.module.pay.controller.admin.channel.dto.PayChannelSaveReqDTO;
import com.hdl.soar.module.pay.dal.entity.channel.PayChannelPO;
import com.hdl.soar.module.pay.mapper.channel.PayChannelMapper;
import com.hdl.soar.module.pay.service.channel.PayChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.hdl.soar.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin Backend - Payment Channel")
@RestController
@RequestMapping("/pay/channel")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayChannelController {

    PayChannelService channelService;

    @PostMapping("/create")
    @Operation(summary = "Create payment channel")
    @PreAuthorize("@ss.hasPermission('pay:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody PayChannelSaveReqDTO createReqDTO) {
        return success(channelService.createChannel(createReqDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update payment channel")
    @PreAuthorize("@ss.hasPermission('pay:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody PayChannelSaveReqDTO updateReqDTO) {
        channelService.updateChannel(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete payment channel")
    @Parameter(name = "id", description = "Channel ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        channelService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get payment channel")
    @Parameter(name = "id", description = "Channel ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<PayChannelRespDTO> getChannel(@RequestParam("id") Long id) {
        PayChannelPO channel = channelService.getChannel(id);
        return success(PayChannelMapper.INSTANCE.toDTO(channel));
    }

    @GetMapping("/page")
    @Operation(summary = "Get payment channel paginated list")
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<PageResult<PayChannelRespDTO>> getChannelPage(@Valid PayChannelPageReqDTO pageReqDTO) {
        PageResult<PayChannelPO> pageResult = channelService.getChannelPage(pageReqDTO);
        return success(new PageResult<>(
                PayChannelMapper.INSTANCE.toDTOList(pageResult.getList()),
                pageResult.getTotal()));
    }

}
