package com.hdl.soar.framework.websocket.config;

import com.hdl.soar.framework.mq.redis.config.SoarRedisMQConsumerAutoConfiguration;
import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import com.hdl.soar.framework.web.config.WebProperties;
import com.hdl.soar.framework.websocket.core.handler.JsonWebSocketMessageHandler;
import com.hdl.soar.framework.websocket.core.listener.WebSocketMessageListener;
import com.hdl.soar.framework.websocket.core.security.LoginUserHandshakeInterceptor;
import com.hdl.soar.framework.websocket.core.security.WebSocketAuthorizeRequestsCustomizer;
import com.hdl.soar.framework.websocket.core.sender.kafka.KafkaWebSocketMessageConsumer;
import com.hdl.soar.framework.websocket.core.sender.kafka.KafkaWebSocketMessageSender;
import com.hdl.soar.framework.websocket.core.sender.local.LocalWebSocketMessageSender;
import com.hdl.soar.framework.websocket.core.sender.rabbitmq.RabbitMQWebSocketMessageConsumer;
import com.hdl.soar.framework.websocket.core.sender.rabbitmq.RabbitMQWebSocketMessageSender;
import com.hdl.soar.framework.websocket.core.sender.redis.RedisWebSocketMessageConsumer;
import com.hdl.soar.framework.websocket.core.sender.redis.RedisWebSocketMessageSender;
import com.hdl.soar.framework.websocket.core.session.WebSocketSessionHandlerDecorator;
import com.hdl.soar.framework.websocket.core.session.WebSocketSessionManager;
import com.hdl.soar.framework.websocket.core.session.WebSocketSessionManagerImpl;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;

/**
 * Wires WebSocket support: the handshake, the message handler, the session manager,
 * the security customizer, and the sender selected by {@code soar.websocket.sender-type}.
 *
 * <p>Declared before {@link SoarRedisMQConsumerAutoConfiguration} so the redis sender's
 * consumer bean exists before the MQ consumer container is built and can register it.
 */
@EnableWebSocket
@AutoConfiguration(before = SoarRedisMQConsumerAutoConfiguration.class)
@ConditionalOnProperty(prefix = "soar.websocket", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(WebSocketProperties.class)
public class SoarWebSocketAutoConfiguration {

    @Bean
    public WebSocketConfigurer webSocketConfigurer(HandshakeInterceptor[] handshakeInterceptors,
                                                   WebSocketHandler webSocketHandler,
                                                   WebSocketProperties webSocketProperties) {
        return registry -> registry
                .addHandler(webSocketHandler, webSocketProperties.getPath())
                .addInterceptors(handshakeInterceptors)
                .setAllowedOriginPatterns("*");
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new LoginUserHandshakeInterceptor();
    }

    @Bean
    public WebSocketHandler webSocketHandler(WebSocketSessionManager sessionManager,
                                             List<? extends WebSocketMessageListener<?>> messageListeners) {
        JsonWebSocketMessageHandler messageHandler = new JsonWebSocketMessageHandler(messageListeners);
        return new WebSocketSessionHandlerDecorator(messageHandler, sessionManager);
    }

    @Bean
    public WebSocketSessionManager webSocketSessionManager() {
        return new WebSocketSessionManagerImpl();
    }

    @Bean
    public WebSocketAuthorizeRequestsCustomizer webSocketAuthorizeRequestsCustomizer(
            WebProperties webProperties, WebSocketProperties webSocketProperties) {
        return new WebSocketAuthorizeRequestsCustomizer(webProperties, webSocketProperties);
    }

    /**
     * Single-node sender. Default when {@code sender-type} is absent.
     */
    @Configuration
    @ConditionalOnProperty(prefix = "soar.websocket", name = "sender-type", havingValue = "local",
            matchIfMissing = true)
    public static class LocalWebSocketMessageSenderConfiguration {

        @Bean
        public LocalWebSocketMessageSender localWebSocketMessageSender(WebSocketSessionManager sessionManager) {
            return new LocalWebSocketMessageSender(sessionManager);
        }

    }

    /**
     * Multi-node sender over Redis pub/sub (built on the Redis MQ starter).
     */
    @Configuration
    @ConditionalOnProperty(prefix = "soar.websocket", name = "sender-type", havingValue = "redis")
    public static class RedisWebSocketMessageSenderConfiguration {

        @Bean
        public RedisWebSocketMessageSender redisWebSocketMessageSender(WebSocketSessionManager sessionManager,
                                                                       RedisMQTemplate redisMQTemplate) {
            return new RedisWebSocketMessageSender(sessionManager, redisMQTemplate);
        }

        @Bean
        public RedisWebSocketMessageConsumer redisWebSocketMessageConsumer(
                RedisWebSocketMessageSender redisWebSocketMessageSender) {
            return new RedisWebSocketMessageConsumer(redisWebSocketMessageSender);
        }

    }

    /**
     * Multi-node sender over RabbitMQ.
     */
    @Configuration
    @ConditionalOnProperty(prefix = "soar.websocket", name = "sender-type", havingValue = "rabbitmq")
    public static class RabbitMQWebSocketMessageSenderConfiguration {

        @Bean
        public RabbitMQWebSocketMessageSender rabbitMQWebSocketMessageSender(
                WebSocketSessionManager sessionManager, RabbitTemplate rabbitTemplate,
                TopicExchange websocketTopicExchange) {
            return new RabbitMQWebSocketMessageSender(sessionManager, rabbitTemplate, websocketTopicExchange);
        }

        @Bean
        public RabbitMQWebSocketMessageConsumer rabbitMQWebSocketMessageConsumer(
                RabbitMQWebSocketMessageSender rabbitMQWebSocketMessageSender) {
            return new RabbitMQWebSocketMessageConsumer(rabbitMQWebSocketMessageSender);
        }

        /**
         * The shared durable topic exchange all nodes bind to.
         */
        @Bean
        public TopicExchange websocketTopicExchange(
                @Value("${soar.websocket.sender-rabbitmq.exchange}") String exchange) {
            return new TopicExchange(exchange, true, false); // durable, not exclusive
        }

    }

    /**
     * Multi-node sender over Kafka.
     */
    @Configuration
    @ConditionalOnProperty(prefix = "soar.websocket", name = "sender-type", havingValue = "kafka")
    public static class KafkaWebSocketMessageSenderConfiguration {

        @Bean
        public KafkaWebSocketMessageSender kafkaWebSocketMessageSender(
                WebSocketSessionManager sessionManager, KafkaTemplate<Object, Object> kafkaTemplate,
                @Value("${soar.websocket.sender-kafka.topic}") String topic) {
            return new KafkaWebSocketMessageSender(sessionManager, kafkaTemplate, topic);
        }

        @Bean
        public KafkaWebSocketMessageConsumer kafkaWebSocketMessageConsumer(
                KafkaWebSocketMessageSender kafkaWebSocketMessageSender) {
            return new KafkaWebSocketMessageConsumer(kafkaWebSocketMessageSender);
        }

    }

}
