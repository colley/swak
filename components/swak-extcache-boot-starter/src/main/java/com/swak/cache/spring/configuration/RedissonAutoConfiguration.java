package com.swak.cache.spring.configuration;

import com.swak.cache.config.RedissonProperties;
import com.swak.cache.redis.SimpleRedisCacheProxy;
import com.swak.core.SwakConstants;
import com.swak.core.cache.DistributedCacheProxy;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Redisson.class})
@ConditionalOnProperty(prefix = "spring.redis.redisson", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({RedissonProperties.class, RedisProperties.class})
public class RedissonAutoConfiguration {

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

    @Resource
    private RedissonProperties redissonProperties;

    @Resource
    private RedisProperties redisProperties;

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
        Method clusterMethod = ReflectionUtils.findMethod(RedisProperties.class, "getCluster");
        Method usernameMethod = ReflectionUtils.findMethod(RedisProperties.class, "getUsername");
        Method timeoutMethod = ReflectionUtils.findMethod(RedisProperties.class, "getTimeout");
        Object timeoutValue = ReflectionUtils.invokeMethod(timeoutMethod, redisProperties);
        int timeout;
        if (null == timeoutValue) {
            timeout = 10000;
        } else if (!(timeoutValue instanceof Integer)) {
            Method millisMethod = ReflectionUtils.findMethod(timeoutValue.getClass(), "toMillis");
            timeout = ((Long) ReflectionUtils.invokeMethod(millisMethod, timeoutValue)).intValue();
        } else {
            timeout = (Integer) timeoutValue;
        }

        String username = null;
        if (usernameMethod != null) {
            username = (String) ReflectionUtils.invokeMethod(usernameMethod, redisProperties);
        }

        if (redissonProperties.getConfig() != null) {
            try {
                config = Config.fromYAML(redissonProperties.getConfig());
            } catch (IOException e) {
                try {
                    config = Config.fromJSON(redissonProperties.getConfig());
                } catch (IOException e1) {
                    e1.addSuppressed(e);
                    throw new IllegalArgumentException("Can't parse config", e1);
                }
            }
        } else if (redissonProperties.getFile() != null) {
            try {
                InputStream is = getConfigStream();
                config = Config.fromYAML(is);
            } catch (IOException e) {
                // trying next format
                try {
                    InputStream is = getConfigStream();
                    config = Config.fromJSON(is);
                } catch (IOException e1) {
                    e1.addSuppressed(e);
                    throw new IllegalArgumentException("Can't parse config", e1);
                }
            }
        } else if (redisProperties.getSentinel() != null) {
            Method nodesMethod = ReflectionUtils.findMethod(RedisProperties.Sentinel.class, "getNodes");
            Object nodesValue = ReflectionUtils.invokeMethod(nodesMethod, redisProperties.getSentinel());

            String[] nodes;
            if (nodesValue instanceof String) {
                nodes = convert(Arrays.asList(((String) nodesValue).split(",")));
            } else {
                nodes = convert((List<String>) nodesValue);
            }

            config = new Config();
            config.useSentinelServers()
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .addSentinelAddress(nodes)
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectTimeout(timeout)
                    .setUsername(username)
                    .setPassword(redisProperties.getPassword());
        } else if (clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null) {
            Object clusterObject = ReflectionUtils.invokeMethod(clusterMethod, redisProperties);
            Method nodesMethod = ReflectionUtils.findMethod(clusterObject.getClass(), "getNodes");
            List<String> nodesObject = (List) ReflectionUtils.invokeMethod(nodesMethod, clusterObject);

            String[] nodes = convert(nodesObject);

            config = new Config();
            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setConnectTimeout(timeout)
                    .setUsername(username)
                    .setPassword(redisProperties.getPassword());
        } else {
            config = new Config();
            String prefix = REDIS_PROTOCOL_PREFIX;
            Method method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
            if (method != null && (Boolean) ReflectionUtils.invokeMethod(method, redisProperties)) {
                prefix = REDISS_PROTOCOL_PREFIX;
            }

            config.useSingleServer()
                    .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                    .setConnectTimeout(timeout)
                    .setDatabase(redisProperties.getDatabase())
                    .setUsername(username)
                    .setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
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
        InputStream is = resource.getInputStream();
        return is;
    }
}
