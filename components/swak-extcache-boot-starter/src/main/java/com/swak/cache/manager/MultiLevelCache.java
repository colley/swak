
package com.swak.cache.manager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.Callable;

public class MultiLevelCache implements Cache {

    private final Cache firstCache;

    private final Cache secondCache;
    
    protected MultiLevelCache(Cache firstCache, Cache secondCache) {
        Assert.notNull(firstCache, "firstCache cannot be null");
        Assert.notNull(secondCache, "secondCache cannot be null");
        this.firstCache = firstCache;
        this.secondCache = secondCache;
    }
    
    @Override
    public void put(Object key, Object value) {
        if (firstCache != null) {
            firstCache.put(key, value);
        }
        if (secondCache != null) {
            secondCache.put(key, value);
        }
    }
    
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper valueWrapper = null;
        if (firstCache != null) {
            valueWrapper = firstCache.putIfAbsent(key, value);
        }
        if (secondCache != null) {
            valueWrapper = secondCache.putIfAbsent(key, value);
        }
        return valueWrapper;
    }
    
    @Override
    public void evict(Object key) {
        if (firstCache != null) {
            firstCache.evict(key);
        }
        if (secondCache != null) {
            secondCache.evict(key);
        }
    }
    
    @Override
    public boolean evictIfPresent(Object key) {
        boolean redisResult = firstCache != null && firstCache.evictIfPresent(key);
        boolean caffeineResult = secondCache != null && secondCache.evictIfPresent(key);
        return redisResult && caffeineResult;
    }
    
    @Override
    public void clear() {
        if (firstCache != null) {
            firstCache.clear();
        }
        if (secondCache != null) {
            secondCache.clear();
        }
    }
    
    @Override
    public boolean invalidate() {
        boolean redisResult = firstCache != null && firstCache.invalidate();
        boolean caffeineResult = secondCache != null && secondCache.invalidate();
        return redisResult && caffeineResult;
    }
    
    @Override
    public String getName() {
        String name = null;
        
        if (firstCache != null) {
            name = firstCache.getName();
        }
        if (StringUtils.isEmpty(name) && secondCache != null) {
            name = secondCache.getName();
        }
        return name;
    }
    
    @Override
    public Object getNativeCache() {
        Object nativeCache = null;
        if (firstCache != null) {
            nativeCache = firstCache.getNativeCache();
        }
        if (Objects.isNull(nativeCache) && secondCache != null) {
            nativeCache = secondCache.getNativeCache();
        }
        return nativeCache;
    }
    
    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper = null;
        if (firstCache != null) {
            valueWrapper = firstCache.get(key);
        }
        if (valueWrapper == null && secondCache != null) {
            valueWrapper = secondCache.get(key);
            // 二级缓存的数据回填至一级缓存
            if (valueWrapper != null && firstCache != null) {
                firstCache.put(key, valueWrapper.get());
            }
        }
        return valueWrapper;
    }
    
    @Override
    public <T> T get(Object key, Class<T> type) {
        T t = null;
        if (firstCache != null) {
            t = firstCache.get(key, type);
        }
        if (Objects.isNull(t) && secondCache != null) {
            t = secondCache.get(key, type);
        }
        return t;
    }
    
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        T t = null;
        if (firstCache != null) {
            t = firstCache.get(key, valueLoader);
        }
        if (Objects.isNull(t) && secondCache != null) {
            t = secondCache.get(key, valueLoader);
        }
        return t;
    }

}
