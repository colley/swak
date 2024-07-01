package com.swak.lock.spring.configuration;

import com.swak.autoconfigure.condition.ConditionalExtOnProperty;
import com.swak.autoconfigure.condition.ConditionalSymbol;
import com.swak.core.SwakConstants;
import com.swak.core.environment.SystemEnvironmentConfigurable;
import com.swak.lock.config.ZookeeperProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@ConditionalOnClass({CuratorFramework.class})
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ZookeeperProperties.class})
@ConditionalExtOnProperty(prefix = SwakConstants.SWAK_ZOOKEEPER, name = "connectionStr",symbol = ConditionalSymbol.IS_NOT_EMPTY)
public class ZookeeperConfiguration {

    @Resource
    private SystemEnvironmentConfigurable systemEnvironment;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework(ZookeeperProperties zookeeperProperties) {
        return CuratorFrameworkFactory.builder()
                .connectString(zookeeperProperties.getConnectionStr())
                .sessionTimeoutMs(zookeeperProperties.getSessionTimeout())
                .connectionTimeoutMs(zookeeperProperties.getConnectionTimeout())
                .retryPolicy(new ExponentialBackoffRetry(zookeeperProperties.getElapsedTime(), zookeeperProperties.getRetryCount()))
                .namespace(zookeeperProperties.getNamespace() + "/" + systemEnvironment.getCurrentEnv())
                .build();
    }
}
