package com.swak.cache.spring.configuration;

import com.swak.cache.redis.SimpleRedisCacheProxy;
import com.swak.core.cache.DistributedCacheProxy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({RedisOperations.class})
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnMissingClass("org.redisson.Redisson")
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class SwakRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "swakRedisTemplate")
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> swakRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(RedisSerializer.string());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(DistributedCacheProxy.class)
    @ConditionalOnBean(RedisTemplate.class)
    public DistributedCacheProxy redisCacheProxy(@Qualifier("swakRedisTemplate")
                                                       RedisTemplate<String, Object> swakRedisTemplate) {
        return new SimpleRedisCacheProxy(swakRedisTemplate);
    }
}
