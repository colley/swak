package com.swak.cache.spring.configuration;

import com.google.common.collect.Lists;
import com.swak.cache.DefaultExtCacheRepository;
import com.swak.cache.config.SwakCacheProperties;
import com.swak.cache.manager.ExtCacheManager;
import com.swak.core.SwakConstants;
import com.swak.core.cache.DistributedCacheProxy;
import com.swak.autoconfigure.condition.ClassBeanFiltering;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableCaching
@Slf4j
@EnableConfigurationProperties(SwakCacheProperties.class)
@Import({RedissonAutoConfiguration.class,SwakRedisAutoConfiguration.class})
@ConditionalOnProperty(prefix = SwakConstants.SWAK_CACHE, name = "enabled", havingValue = "true")
public class SwakCacheAutoConfiguration extends CachingConfigurerSupport {

    @Autowired(required = false)
    private DistributedCacheProxy redisCacheProxy;

    @Resource
    private SwakCacheProperties swakCacheProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public CacheManager cacheManager() {
        return getCacheManager();
    }

    /**
     * 如果cache出错， 我们会记录在日志里，方便排查，比如反序列化异常
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new SwakCacheAutoConfiguration.LoggingCacheErrorHandler();
    }

    /* non-public */
    static class LoggingCacheErrorHandler extends SimpleCacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.error(String.format("cacheName:%s,cacheKey:%s", cache == null ? "unknown" : cache.getName(), key),
                    exception);
            super.handleCacheGetError(exception, cache, key);
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.error(String.format("cacheName:%s,cacheKey:%s", cache == null ? "unknown" : cache.getName(), key),
                    exception);
            super.handleCachePutError(exception, cache, key, value);
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.error(String.format("cacheName:%s,cacheKey:%s", cache == null ? "unknown" : cache.getName(), key),
                    exception);
            super.handleCacheEvictError(exception, cache, key);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.error(String.format("cacheName:%s", cache == null ? "unknown" : cache.getName()), exception);
            super.handleCacheClearError(exception, cache);
        }
    }

    /**
     * <bean id="ehcache" class=
     * "org.springframework.cache.ehcache.EhCacheManagerFactoryBean">
     * <property name="configLocation" value="classpath:ehcache.xml"></property>
     * </bean>
     * <p>
     * <bean id="cacheManager" class=
     * "org.springframework.cache.ehcache.EhCacheCacheManager">
     * <property name="cacheManager" ref="ehcache"></property> </bean>
     *
     * @return
     */
    private CacheManager getEhcacheManager() {
        if (StringUtils.isNotEmpty(swakCacheProperties.getEhcachePath())) {
            return null;
        }
        List<String> onMissingClasses = ClassBeanFiltering.filter(applicationContext,
                Lists.newArrayList("net.sf.ehcache.CacheManager"),
                ClassBeanFiltering.ClassNameFilter.MISSING);
        if (CollectionUtils.isNotEmpty(onMissingClasses)) {
            return null;
        }
        EhCacheManagerFactoryBean ehcache = new EhCacheManagerFactoryBean();
        PathMatchingResourcePatternResolver pathMatchResolver = new PathMatchingResourcePatternResolver();
        ehcache.setConfigLocation(pathMatchResolver.getResource(swakCacheProperties.getEhcachePath()));
        EhCacheCacheManager cacheManager = new EhCacheCacheManager(ehcache.getObject());
        cacheManager.afterPropertiesSet();
        return cacheManager;
    }

    private CacheManager getCaffeineCacheManager() {
        return new CaffeineCacheManager();
    }

    @Bean("cacheManager")
    @ConditionalOnMissingBean
    public CacheManager getCacheManager() {
        ExtCacheManager.Builder builder = new ExtCacheManager.Builder();
        //配置了 Ehcache 就使用为默认的，反之使用Caffeine
        builder.defaultCacheManager(Optional.ofNullable(getEhcacheManager()).orElse(getCaffeineCacheManager()));
        builder.redisCacheProxy(redisCacheProxy).cacheRepository(new DefaultExtCacheRepository());
        return builder.build();
    }
}
