package com.swak.cache.spring.configuration;

import com.swak.cache.config.RedissonProperties;
import com.swak.cache.redis.SimpleRedisCacheProxy;
import com.swak.core.cache.DistributedCacheProxy;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Redisson 自动配置。
 * <p>
 * 适配点（Spring Boot 4 + Redisson 4）：
 * <ul>
 *     <li>{@code RedisProperties} 已更名为 {@code DataRedisProperties}，且 SSL 由 {@code isSsl()} 改为
 *     {@code getSsl().isEnabled()}，不再需要反射；</li>
 *     <li>Redisson 4.x 移除了 {@code Config.fromJSON}，只支持 YAML；</li>
 *     <li>Redisson 4.x 的账号密码从各 ServerConfig 迁移到 {@link Config} 级别。</li>
 * </ul>
 *
 * @author colley.ma
 * @since 3.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Redisson.class})
@ConditionalOnProperty(prefix = "spring.redis.redisson", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({RedissonProperties.class, DataRedisProperties.class})
public class RedissonAutoConfiguration {

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;

    @Resource
    private RedissonProperties redissonProperties;

    @Resource
    private DataRedisProperties redisProperties;

    @Resource
    private ApplicationContext ctx;

    @Bean
    @ConditionalOnMissingBean(name = "swakRedisTemplate")
    public RedisTemplate<String, Object> swakRedisTemplate(@Autowired(required = false) RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(RedisSerializer.string());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(DistributedCacheProxy.class)
    public DistributedCacheProxy redisCacheProxy(@Autowired(required = false) RedisTemplate<String, Object> swakRedisTemplate) {
        return new SimpleRedisCacheProxy(swakRedisTemplate);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() throws IOException {
        Config config;
        if (redissonProperties.getConfig() != null) {
            config = parseYaml(redissonProperties.getConfig());
        } else if (redissonProperties.getFile() != null) {
            try (InputStream is = getConfigStream()) {
                config = parseYaml(is);
            }
        } else {
            config = buildConfigFromRedisProperties();
            // Redisson 4.x 起账号密码统一下沉到 Config
            config.setUsername(redisProperties.getUsername());
            config.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    private Config buildConfigFromRedisProperties() {
        Duration timeoutValue = redisProperties.getTimeout();
        int timeout = timeoutValue == null ? DEFAULT_TIMEOUT_MILLIS : (int) timeoutValue.toMillis();

        if (redisProperties.getSentinel() != null) {
            DataRedisProperties.Sentinel sentinel = redisProperties.getSentinel();
            Config config = new Config();
            config.useSentinelServers()
                    .setMasterName(sentinel.getMaster())
                    .addSentinelAddress(convert(sentinel.getNodes()))
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectTimeout(timeout);
            return config;
        }

        if (redisProperties.getCluster() != null) {
            Config config = new Config();
            config.useClusterServers()
                    .addNodeAddress(convert(redisProperties.getCluster().getNodes()))
                    .setConnectTimeout(timeout);
            return config;
        }

        String prefix = redisProperties.getSsl().isEnabled() ? REDISS_PROTOCOL_PREFIX : REDIS_PROTOCOL_PREFIX;
        Config config = new Config();
        config.useSingleServer()
                .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setConnectTimeout(timeout)
                .setDatabase(redisProperties.getDatabase());
        return config;
    }

    /**
     * Redisson 4.x 已移除 {@code Config.fromJSON}，仅支持 YAML。
     */
    private Config parseYaml(String configText) {
        try {
            return Config.fromYAML(configText);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Can't parse redisson config, only YAML format is supported since Redisson 4.x", e);
        }
    }

    /**
     * Redisson 4.x 已移除 {@code Config.fromJSON}，仅支持 YAML。
     */
    private Config parseYaml(InputStream inputStream) {
        try {
            return Config.fromYAML(inputStream);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Can't parse redisson config, only YAML format is supported since Redisson 4.x", e);
        }
    }

    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<String>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
                nodes.add(REDIS_PROTOCOL_PREFIX + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }

    private InputStream getConfigStream() throws IOException {
        org.springframework.core.io.Resource resource = ctx.getResource(redissonProperties.getFile());
        return resource.getInputStream();
    }
}
