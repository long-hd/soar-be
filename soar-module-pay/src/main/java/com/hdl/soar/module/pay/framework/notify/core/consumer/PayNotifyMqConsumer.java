package com.hdl.soar.module.pay.framework.notify.core.consumer;

import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.module.pay.framework.notify.core.message.PayNotifyMessage;
import com.hdl.soar.module.pay.service.notify.PayNotifyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * The single place outbound notify HTTP is dispatched. Pulls a task id off the work queue and hands
 * it to {@link PayNotifyService#deliverNotifyTask}, which restores tenant context, takes the per-task
 * lock, performs the HTTP call, and records the result.
 * <p>
 * A merchant failure is a recorded outcome, not a message failure, so the delivery call returns
 * normally and the message is acked — the retry is a fresh message emitted later by the poll job.
 * The listener only lets an exception escape (-> dead-letter, because
 * {@code defaultRequeueRejected=false}) for a bad payload or an infrastructure failure it cannot
 * record; the task stays {@code WAITING} and the poll job recovers it.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayNotifyMqConsumer {

    PayNotifyService payNotifyService;

    @RabbitListener(queues = "${soar.pay.notify.queue}",
            containerFactory = "payNotifyRabbitListenerContainerFactory")
    public void onMessage(String body) {
        PayNotifyMessage message = JsonUtils.parseObject(body, PayNotifyMessage.class);
        if (message == null || message.taskId() == null) {
            // Unparseable / malformed -> park in the DLQ, do not requeue.
            throw new AmqpRejectAndDontRequeueException("invalid pay-notify message: " + body);
        }
        payNotifyService.deliverNotifyTask(message.taskId(), message.tenantId());
    }

}
