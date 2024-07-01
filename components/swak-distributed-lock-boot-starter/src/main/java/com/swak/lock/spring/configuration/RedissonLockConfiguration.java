package com.swak.lock.spring.configuration;

import com.swak.cache.spring.configuration.SwakCacheAutoConfiguration;
import com.swak.core.SwakConstants;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.config.LockProperties;
import com.swak.lock.sync.RedissonLock;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Redisson.class})
@AutoConfigureBefore(SwakCacheAutoConfiguration.class)
@EnableConfigurationProperties(LockProperties.class)
@ConditionalOnProperty(prefix = SwakConstants.SWAK_LOCK, name = "type", havingValue = "redisson")
public class RedissonLockConfiguration {

    @Resource
    private LockProperties lockProperties;

    @Bean
    @ConditionalOnMissingBean(DistributedLock.class)
    public DistributedLock distributedLock(RedissonClient redissonClient) {
        return new RedissonLock(redissonClient,lockProperties);
    }
}
