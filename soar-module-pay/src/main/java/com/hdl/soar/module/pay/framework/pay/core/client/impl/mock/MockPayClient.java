package com.hdl.soar.module.pay.framework.pay.core.client.impl.mock;

import com.hdl.soar.module.pay.enums.order.PayOrderStatusEnum;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.PayOrderGetReqDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderChannelRespDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import com.hdl.soar.module.pay.framework.pay.core.client.impl.AbstractPayClient;
import com.hdl.soar.module.pay.framework.pay.core.client.impl.NonePayClientConfig;

import java.time.Instant;
import java.util.Map;

/**
 * A fake rail that succeeds instantly. Lets the whole submit/notify flow be exercised without a real
 * channel: {@link #doUnifiedOrder} returns {@code SUCCESS} on the spot, so the state machine marks the
 * order paid immediately.
 * <p>
 * A real rail has no synchronous success and no self-issued callback; {@link #doParseOrderNotify} here
 * reads {@code outTradeNo}/{@code status} straight from the request so the callback endpoint can be
 * curl-tested. The real signature-verifying parse arrives with VNPay.
 */
public class MockPayClient extends AbstractPayClient<NonePayClientConfig> {

    private static final String MOCK_RAW_DATA = "MOCK_SUCCESS";

    public MockPayClient(Long channelId, NonePayClientConfig config) {
        super(channelId, config);
    }

    @Override
    protected void doInit() {
        // no-op
    }

    @Override
    protected PayOrderChannelRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Throwable {
        return PayOrderChannelRespDTO.successOf("MOCK-" + reqDTO.getOutTradeNo(), "mock-user",
                Instant.now(), reqDTO.getOutTradeNo(), MOCK_RAW_DATA);
    }

    @Override
    protected PayOrderChannelRespDTO doParseOrderNotify(Map<String, String> params, String body,
                                                        Map<String, String> headers) throws Throwable {
        String outTradeNo = params.get("outTradeNo");
        String status = params.get("status");
        if (String.valueOf(PayOrderStatusEnum.CLOSED.getStatus()).equals(status)) {
            return PayOrderChannelRespDTO.closedOf("MOCK_CLOSED", "mock closed", outTradeNo, body);
        }
        return PayOrderChannelRespDTO.successOf("MOCK-" + outTradeNo, "mock-user",
                Instant.now(), outTradeNo, body);
    }

    @Override
    protected PayOrderChannelRespDTO doGetOrder(PayOrderGetReqDTO reqDTO) throws Throwable {
        // mock ignores createTime; always reports SUCCESS so the reconcile flow is testable offline
        String outTradeNo = reqDTO.getOutTradeNo();
        return PayOrderChannelRespDTO.successOf("MOCK-" + outTradeNo, "mock-user",
                Instant.now(), outTradeNo, MOCK_RAW_DATA);
    }

}
