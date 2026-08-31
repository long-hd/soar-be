package com.hdl.soar.module.pay.framework.notify;

import com.hdl.soar.module.pay.framework.notify.core.producer.PayNotifyProducer;
import com.hdl.soar.module.pay.framework.pay.config.PayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Producer never throws on a broker outage, and targets the configured exchange + routing key. */
public class PayNotifyProducerTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final PayProperties payProperties = new PayProperties();
    private PayNotifyProducer producer;

    @BeforeEach
    void setUp() {
        PayProperties.Notify notify = new PayProperties.Notify();
        notify.setExchange("ex");
        payProperties.setNotify(notify);
        producer = new PayNotifyProducer(rabbitTemplate, payProperties);
    }

    @Test
    @DisplayName("publishes to the configured exchange with the pay.notify routing key")
    void publish_sendsToExchangeWithRoutingKey() {
        boolean ok = producer.publish(1L, 2L);

        assertThat(ok).isTrue();
        verify(rabbitTemplate).convertAndSend(eq("ex"), eq(PayNotifyProducer.ROUTING_KEY), anyString());
    }

    @Test
    @DisplayName("swallows a broker failure and returns false so the poll job can retry")
    void publish_onBrokerFailure_returnsFalse() {
        doThrow(new AmqpException("broker down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        boolean ok = producer.publish(1L, 2L); // must not throw

        assertThat(ok).isFalse();
    }

}
