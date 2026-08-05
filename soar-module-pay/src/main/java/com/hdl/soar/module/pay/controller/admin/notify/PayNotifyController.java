package com.hdl.soar.module.pay.controller.admin.notify;

import cn.hutool.core.map.MapUtil;
import com.hdl.soar.framework.tenant.core.aop.TenantIgnore;
import com.hdl.soar.module.pay.framework.pay.core.client.PayClient;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;
import com.hdl.soar.module.pay.service.channel.PayChannelService;
import com.hdl.soar.module.pay.service.order.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.Map;

/**
 * Receives channel callbacks (IPN). The rail calls the {@code notifyUrl} we handed it at submit time,
 * which is this endpoint with the channel id appended.
 * <p>
 * {@code @PermitAll}: a rail cannot log in — the callback is authenticated by the channel's own
 * signature, verified inside {@code parseOrderNotify}. The mock client accepts unsigned params so the
 * endpoint can be tested; VNPay verifies a real HMAC.
 * <p>
 * {@code @TenantIgnore}: the callback arrives with no tenant context, but {@code getPayClient} loads
 * the tenant-scoped channel — so the whole request must run with the tenant filter off; the real
 * tenant is derived from the channel inside {@code notifyOrder}.
 */
@Tag(name = "App - Payment Callback")
@Slf4j
@RestController
@RequestMapping("/pay/notify")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayNotifyController {

    PayChannelService channelService;
    PayOrderService orderService;

    @RequestMapping(value = "/order/{channelId}", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Channel order callback")
    @PermitAll
    @TenantIgnore
    public String notifyOrder(@PathVariable("channelId") Long channelId,
                              @RequestParam(required = false) Map<String, String> params,
                              @RequestBody(required = false) String body,
                              HttpServletRequest request) {
        log.info("[notifyOrder][channel({}) params({})]", channelId, params);
        PayClient<?> client = channelService.getPayClient(channelId);
        PayOrderChannelRespDTO notify = client.parseOrderNotify(
                params != null ? params : MapUtil.newHashMap(), body, extractHeaders(request));
        orderService.notifyOrder(channelId, notify);
        return "success";
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = MapUtil.newHashMap();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }

}
