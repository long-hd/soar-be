package com.hdl.soar.module.pay.framework.notify;

import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.module.pay.framework.notify.core.consumer.PayNotifyMqConsumer;
import com.hdl.soar.module.pay.framework.notify.core.message.PayNotifyMessage;
import com.hdl.soar.module.pay.service.notify.PayNotifyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class PayNotifyMqConsumerTest {

    private final PayNotifyService payNotifyService = mock(PayNotifyService.class);
    private final PayNotifyMqConsumer consumer = new PayNotifyMqConsumer(payNotifyService);

    @Test
    @DisplayName("a valid message is delivered with its task id and tenant id")
    void onMessage_valid_delegates() {
        String body = JsonUtils.toJsonString(new PayNotifyMessage(11L, 22L));

        consumer.onMessage(body);

        verify(payNotifyService).deliverNotifyTask(11L, 22L);
    }

    @Test
    @DisplayName("unparseable body is rejected to the DLQ, never delivered")
    void onMessage_unparseable_rejects() {
        assertThatThrownBy(() -> consumer.onMessage("}{ not json"))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(payNotifyService);
    }

    @Test
    @DisplayName("a message without a task id is rejected, never delivered")
    void onMessage_missingTaskId_rejects() {
        assertThatThrownBy(() -> consumer.onMessage("{\"tenantId\":22}"))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(payNotifyService);
    }

}
