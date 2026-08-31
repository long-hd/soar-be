package com.hdl.soar.module.pay.framework.notify.core.producer;

import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.module.pay.framework.notify.core.message.PayNotifyMessage;
import com.hdl.soar.module.pay.framework.pay.config.PayProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes a notify task onto the pay-notify work queue.
 * <p>
 * Called in two places, both AFTER the outbox row is committed: the afterCommit fast-path (low
 * latency) and the poll job (backstop + retry driver). The message is a small JSON string produced
 * by {@link JsonUtils}, so no global {@code MessageConverter} bean is introduced (which would also
 * change the websocket-rabbit wire format).
 * <p>
 * Publish failures are swallowed and logged: the task stays {@code WAITING} in the database, and the
 * poll job re-publishes it later. This is what keeps the outbox honest — the broker is a transport,
 * not the source of truth.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayNotifyProducer {

    /** Routing key bound to the pay-notify queue on the direct exchange. */
    public static final String ROUTING_KEY = "pay.notify";

    RabbitTemplate rabbitTemplate;
    PayProperties payProperties;

    /**
     * Publish one task for delivery. Never throws: a broker outage here degrades to "delivered late
     * by the poll job", never to a failed post-commit callback.
     *
     * @return {@code true} if the message was handed to the broker, {@code false} on publish failure
     */
    public boolean publish(Long taskId, Long tenantId) {
        String exchange = payProperties.getNotify().getExchange();
        try {
            rabbitTemplate.convertAndSend(exchange, ROUTING_KEY,
                    JsonUtils.toJsonString(new PayNotifyMessage(taskId, tenantId)));
            return true;
        } catch (Exception ex) {
            log.warn("[publish][task({}) publish failed; poll will retry]", taskId, ex);
            return false;
        }
    }

}
