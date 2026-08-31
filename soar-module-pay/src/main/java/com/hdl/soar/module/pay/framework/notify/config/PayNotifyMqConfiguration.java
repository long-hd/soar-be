package com.hdl.soar.module.pay.framework.notify.config;

import com.hdl.soar.module.pay.framework.notify.core.producer.PayNotifyProducer;
import com.hdl.soar.module.pay.framework.pay.config.PayProperties;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the pay-notify work-queue topology and a dedicated listener container factory.
 * <p>
 * Topology (all durable): a direct exchange, one shared work queue whose {@code x-dead-letter-exchange}
 * points at a fanout dead-letter exchange, and a dead-letter queue for parking. Unlike the
 * websocket-rabbit broadcast (topic + per-node auto-delete queue), this is a single durable queue
 * drained competitively by consumers — a task is delivered by exactly one of them.
 * <p>
 * The dead-letter path is a parking lot for observability, NOT a retry engine: retry timing lives in
 * the database (see {@code PayNotifyServiceImpl}). A message only dead-letters when the consumer
 * cannot record an outcome at all (bad payload, or an infrastructure failure) — see
 * {@link #payNotifyRabbitListenerContainerFactory}.
 */
@Configuration(proxyBeanMethods = false)
public class PayNotifyMqConfiguration {

    @Bean
    public DirectExchange payNotifyExchange(PayProperties payProperties) {
        return new DirectExchange(payProperties.getNotify().getExchange(),
                true,
                false); // durable, not auto-delete
    }

    @Bean
    public FanoutExchange payNotifyDeadLetterExchange(PayProperties payProperties) {
        return new FanoutExchange(payProperties.getNotify().getDeadLetterExchange(),
                true,
                false);
    }

    /** Shared, durable work queue; rejected messages route to the dead-letter exchange. */
    @Bean
    public Queue payNotifyQueue(PayProperties payProperties) {
        return QueueBuilder.durable(payProperties.getNotify().getQueue())
                .withArgument("x-dead-letter-exchange", payProperties.getNotify().getDeadLetterExchange())
                .build();
    }

    @Bean
    public Queue payNotifyDeadLetterQueue(PayProperties payProperties) {
        return QueueBuilder.durable(payProperties.getNotify().getDeadLetterQueue())
                .build();
    }

    @Bean
    public Binding payNotifyBinding(Queue payNotifyQueue, DirectExchange payNotifyExchange) {
        return BindingBuilder.bind(payNotifyQueue)
                .to(payNotifyExchange)
                .with(PayNotifyProducer.ROUTING_KEY);
    }

    /** Fanout: any dead-lettered message lands in the DLQ regardless of its original routing key. */
    @Bean
    public Binding payNotifyDeadLetterBinding(Queue payNotifyDeadLetterQueue, FanoutExchange payNotifyDeadLetterExchange) {
        return BindingBuilder.bind(payNotifyDeadLetterQueue)
                .to(payNotifyDeadLetterExchange);
    }

    /**
     * Dedicated factory so pay-notify's error semantics do NOT leak onto other listeners (e.g.
     * websocket). {@code defaultRequeueRejected=false} means a thrown listener exception rejects the
     * message to the DLX instead of requeuing it forever (poison-loop guard). Concurrency here — not
     * a prefetch spike — is what parallelises the I/O-bound HTTP delivery.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory payNotifyRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(8);
        factory.setConcurrentConsumers(4);
        factory.setMaxConcurrentConsumers(8);
        return factory;
    }

}
