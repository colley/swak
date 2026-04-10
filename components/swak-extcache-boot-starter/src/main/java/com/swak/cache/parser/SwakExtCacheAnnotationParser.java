
package com.swak.cache.parser;

import com.swak.cache.DefaultExtCacheRepository;
import com.swak.cache.annotation.*;
import com.swak.core.aware.SwakPostProcessor;
import com.swak.core.interceptor.SwakAnnotationParser;
import com.swak.core.interceptor.SwakAnnotationUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Cacheable 扩展 ExtCacheable解析
 */
public class SwakExtCacheAnnotationParser implements SwakAnnotationParser<ExtCacheOperation>, SwakPostProcessor {

    private static final Set<Class<? extends Annotation>> CACHE_OPERATION_ANNOTATIONS = new LinkedHashSet<>(1);
    static {
        CACHE_OPERATION_ANNOTATIONS.add(ExtCacheable.class);
    }

    /**
     * {@link ExtCacheable} not support class,@Target is method
     */
    @Override
    public Collection<ExtCacheOperation> parseAnnotations(Class<?> type) {
        return Collections.emptyList();
    }

    @Override
    public Collection<ExtCacheOperation> parseAnnotations(Method method, Class<?> targetClass) {
        Collection<? extends Annotation> anns =
            SwakAnnotationUtils.computeOperations(method, targetClass, CACHE_OPERATION_ANNOTATIONS);
        if (anns == null) {
            return null;
        }
        final Collection<ExtCacheOperation> ops = new ArrayList<>(1);
        anns.stream().filter(ann -> ann instanceof ExtCacheable)
            .forEach(ann -> ops.add(parseExtCacheableAnnotation(method, (ExtCacheable) ann)));
        return ops;
    }

    private ExtCacheOperation parseExtCacheableAnnotation(AnnotatedElement ae, ExtCacheable cacheable) {
        ExtCacheOperation.Builder builder = new ExtCacheOperation.Builder();
        builder.name(ae.toString());
        builder.caffeine(
            ArrayUtils.isNotEmpty(cacheable.caffeine()) ? parseCaffeineOperation(cacheable.caffeine()[0]) : null);

        builder.redis(ArrayUtils.isNotEmpty(cacheable.redis()) ? parseRedisOperation(cacheable.redis()[0]) : null);

        builder.cacheNames(cacheable.cacheNames());
        builder.condition(cacheable.condition());
        builder.unless(cacheable.unless());
        builder.key(cacheable.key());
        builder.keyGenerator(cacheable.keyGenerator());
        builder.cacheManager(cacheable.cacheManager());
        builder.cacheResolver(cacheable.cacheResolver());
        builder.multiLevel(cacheable.multiLevel());
        ExtCacheOperation op = builder.build();
        validateCacheOperation(ae, op);

        return op;
    }

    private CaffeineOperation parseCaffeineOperation(Caffeine caffeine) {
        CaffeineOperation.Builder builder = new CaffeineOperation.Builder();
        builder.maximumSize(caffeine.maximumSize()).maximumWeight(caffeine.maximumWeight())
            .weigher4MaximumWeight(caffeine.weigher4MaximumWeight()).initialCapacity(caffeine.initialCapacity())
            .refreshAfterWrite(caffeine.refreshAfterWrite()).timeUnit4Refresh(caffeine.timeUnit4Refresh())
            .cacheLoader4Refresh(caffeine.cacheLoader4Refresh()).recordStats(caffeine.recordStats())
            .expireTime(caffeine.expireTime()).timeUnit(caffeine.timeUnit()).expireStrategy(caffeine.expireStrategy())
            .build();
        return builder.build();
    }

    private RedisOperation parseRedisOperation(Redis redis) {
        RedisOperation.Builder builder = new RedisOperation.Builder();
        builder.useRedisTemplate(redis.useRedisTemplate()).expireTime(redis.expireTime()).timeUnit(redis.timeUnit());
        return builder.build();
    }

    private void validateCacheOperation(AnnotatedElement ae, ExtCacheOperation operation) {
        if (StringUtils.hasText(operation.getKey()) && StringUtils.hasText(operation.getKeyGenerator())) {
            throw new IllegalStateException("[Swak-Cache] Invalid cache annotation configuration on '" + ae.toString()
                + "'. Both 'key' and 'keyGenerator' attributes have been set. "
                + "These attributes are mutually exclusive: either set the SpEL expression used to"
                + "compute the key at runtime or set the name of the KeyGenerator bean to use.");
        }
        if (StringUtils.hasText(operation.getCacheManager()) && StringUtils.hasText(operation.getCacheResolver())) {
            throw new IllegalStateException("[Swak-Cache] Invalid cache annotation configuration on '" + ae.toString()
                + "'. Both 'cacheManager' and 'cacheResolver' attributes have been set. "
                + "These attributes are mutually exclusive: the cache manager is used to configure a"
                + "default cache resolver if none is set. If a cache resolver is set, the cache manager"
                + "won't be used.");
        }
    }

    @Override
    public void postBeanAfterInitialization(Object bean, String beanName) {
        // 在处理
        Class<?> clazz = bean.getClass();
        // ExtCacheable注解是基于代理实现的， 不可见的方法是不会走代理的， 这里就没必要使用getDeclaredMethods()来获取全量的了
        Method[] accessibleMethodArray = clazz.getMethods();
        if (ArrayUtils.isEmpty(accessibleMethodArray)) {
            return;
        }
        Arrays.stream(accessibleMethodArray).forEach(method -> {
            postMethodAfterInitialization(method, clazz, bean, beanName);
        });
    }

    protected void postMethodAfterInitialization(Method method, Class<?> targetClass, Object bean, String beanName) {
        Collection<ExtCacheOperation> extCacheAnns = parseAnnotations(method, targetClass);
        if(extCacheAnns!=null) {
            DefaultExtCacheRepository.doRegistration(extCacheAnns);
        }
    }
}
