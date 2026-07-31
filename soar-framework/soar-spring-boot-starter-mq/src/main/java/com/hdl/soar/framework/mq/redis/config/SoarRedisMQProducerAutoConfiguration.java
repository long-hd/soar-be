package com.hdl.soar.framework.mq.redis.config;

import com.hdl.soar.framework.mq.redis.core.RedisMQTemplate;
import com.hdl.soar.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import com.hdl.soar.framework.redis.config.SoarRedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Registers the {@link RedisMQTemplate} used to produce messages, wired with all
 * {@link RedisMessageInterceptor} beans present in the context.
 */
@Slf4j
@AutoConfiguration(after = SoarRedisAutoConfiguration.class)
public class SoarRedisMQProducerAutoConfiguration {

    /**
     * @param redisTemplate a string-serializing template; see {@link RedisMQTemplate}'s
     *                      note on avoiding double serialization
     * @param interceptors  interceptor beans to register, in bean order
     * @return the configured template
     */
    @Bean
    public RedisMQTemplate redisMQTemplate(StringRedisTemplate redisTemplate,
                                           List<RedisMessageInterceptor> interceptors) {
        RedisMQTemplate template = new RedisMQTemplate(redisTemplate);
        interceptors.forEach(template::addInterceptor);
        return template;
    }

}
