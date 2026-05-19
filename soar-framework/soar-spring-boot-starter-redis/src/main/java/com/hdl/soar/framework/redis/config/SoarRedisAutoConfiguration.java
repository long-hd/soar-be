package com.hdl.soar.framework.redis.config;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis configuration class
 */
@AutoConfiguration(before = RedissonAutoConfigurationV2.class) // Purpose: use a custom RedisTemplate Bean
public class SoarRedisAutoConfiguration {
    /**
     * Create a RedisTemplate Bean using JSON serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // Create RedisTemplate object
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // Set RedisConnection factory.
        // 😈 It is the “secret factory” that enables integration with multiple Java Redis clients.
        // If you're interested, you can explore its implementation yourself.
        template.setConnectionFactory(factory);

        // Use String serialization for keys.
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // Use JSON serialization (Jackson) for values.
        template.setValueSerializer(buildRedisSerializer());
        template.setHashValueSerializer(buildRedisSerializer());

        return template;
    }

    public static RedisSerializer<?>  buildRedisSerializer() {
        RedisSerializer<Object> json = RedisSerializer.json();

        // Fix serialization for LocalDateTime
        ObjectMapper objectMapper = (ObjectMapper) ReflectUtil.getFieldValue(json, "mapper");
        objectMapper.registerModules(new JavaTimeModule());

        return json;
    }
}
