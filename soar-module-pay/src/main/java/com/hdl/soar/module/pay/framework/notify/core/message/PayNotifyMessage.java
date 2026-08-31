package com.hdl.soar.module.pay.framework.notify.core.message;

/**
 * Relay envelope for one outbox notify task.
 * <p>
 * Carries only identifiers, never the task body: the database row is the source of truth, so the
 * consumer reloads the task by {@code taskId} and always acts on its latest state (important when a
 * message has sat in the queue). {@code tenantId} lets the consumer restore tenant context across
 * the async boundary without a separate AMQP header.
 *
 * @param taskId   id of the {@code pay_notify_task} row to deliver
 * @param tenantId tenant that owns the task (the notify task table is tenant-scoped)
 */
public record PayNotifyMessage(Long taskId, Long tenantId) {
}
