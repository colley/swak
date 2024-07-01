package com.swak.lock.spring.configuration;

import com.swak.core.SwakConstants;
import com.swak.core.environment.SystemEnvironmentConfigurable;
import com.swak.core.sync.DistributedLock;
import com.swak.lock.config.LockProperties;
import com.swak.lock.config.ZookeeperProperties;
import com.swak.lock.sync.ZookeeperLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.redisson.Redisson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@ConditionalOnBean(CuratorFramework.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({LockProperties.class})
@ConditionalOnProperty(prefix = SwakConstants.SWAK_LOCK, name = "type", havingValue = "zk")
public class ZookeeperLockConfiguration {

    @Bean
    @ConditionalOnMissingBean(DistributedLock.class)
    public DistributedLock distributedLock(CuratorFramework curatorFramework, LockProperties lockProperties) {
        return new ZookeeperLock(curatorFramework, lockProperties);
    }
}
