package com.swak.cache.spring.configuration;

import com.swak.cache.spi.DelayedQueueManager;
import com.swak.cache.queue.RedissonDelayedQueueManager;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DelayedQueueAutoConfiguration.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(RedissonClient.class)
public class DelayedQueueAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DelayedQueueManager.class)
    public DelayedQueueManager delayedQueueManager(RedissonClient redissonClient) {
        return new RedissonDelayedQueueManager(redissonClient);
    }
}
