package com.swak.lock.spring.configuration;

import com.swak.core.SwakConstants;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.config.LockProperties;
import com.swak.lock.sync.LocalSyncLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LockProperties.class)
@ConditionalOnProperty(prefix = SwakConstants.SWAK_LOCK, name = "type", havingValue = "local_only")
public class LocalOnlyLockConfiguration {

    @Bean
    @ConditionalOnMissingBean(DistributedLock.class)
    public DistributedLock distributedLock() {
        return new LocalSyncLock();
    }
}
