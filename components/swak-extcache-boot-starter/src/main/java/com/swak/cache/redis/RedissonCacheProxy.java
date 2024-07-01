//package com.swak.cache.redis;
//
//import com.github.benmanes.caffeine.cache.Cache;
//import com.github.benmanes.caffeine.cache.Caffeine;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.swak.common.exception.SwakAssert;
//import com.swak.core.cache.DistributedCacheProxy;
//import com.swak.core.script.SwakScript;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.collections4.MapUtils;
//import org.apache.commons.lang3.ArrayUtils;
//import org.redisson.api.RBucket;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.redis.core.RedisOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SessionCallback;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.data.redis.core.script.RedisScript;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//public class RedissonCacheProxy implements DistributedCacheProxy, InitializingBean {
//
//    private RedissonClient redissonClient;
//
//    private Cache<String,RedisScript> SCRIPT_CACHE = Caffeine.newBuilder()
//            .maximumSize(1000).build();
//
//    public RedissonCacheProxy(RedissonClient redissonClient) {
//        this.redissonClient = redissonClient;
//    }
//
//    @Override
//    public <T> T getObject(String key) {
//        RBucket<Object> bucket = redissonClient.getBucket(key);
//        return (T) bucket.get();
//    }
//
//    @Override
//    public <T> boolean setObject(String key, T value, long timeout, TimeUnit unit) {
//        RBucket<Object> bucket = redissonClient.getBucket(key);
//        bucket.set(value, timeout, unit);
//        return true;
//    }
//
//
//    @Override
//    public <T> boolean setObject(String key, T value) {
//        RBucket<Object> bucket = redissonClient.getBucket(key);
//        bucket.set(value);
//        return true;
//    }
//
//
//    @Override
//    public Boolean exists(String key) {
//        return redissonClient.getKeys().countExists(key) >0;
//    }
//
//    @Override
//    public Boolean expire(String key, long timeout, TimeUnit unit) {
//        RBucket<Object> bucket = redissonClient.getBucket(key);
//        bucket.expire(Duration.of(timeout, ChronoUnit.SECONDS))
//        return redisTemplate.expire(key, timeout, unit);
//    }
//
//    @Override
//    public Boolean hExists(String key, String field) {
//        return redisTemplate.opsForHash().hasKey(key, field);
//    }
//
//    @Override
//    public <T> Boolean sAdd(String key, T value) {
//        redisTemplate.opsForSet().add(key, value);
//        return true;
//    }
//
//    @Override
//    public <T> Boolean sIsMember(String key, T value) {
//        return redisTemplate.opsForSet().isMember(key, value);
//    }
//
//    @Override
//    public Long sCard(String key) {
//        return redisTemplate.opsForSet().size(key);
//    }
//
//
//    @Override
//    public Long delete(String... keys) {
//        return redisTemplate.delete(Lists.newArrayList(keys));
//    }
//
//    @Override
//    public Long decrBy(String key, long delta) {
//        return redisTemplate.opsForValue().decrement(key, delta);
//    }
//
//    @Override
//    public Long incr(String key) {
//        return redisTemplate.opsForValue().increment(key);
//    }
//
//    @Override
//    public Long incrBy(String key, long delta) {
//        return redisTemplate.opsForValue().increment(key, delta);
//    }
//
//    @Override
//    public Boolean set(String key, String value, long timeout, TimeUnit unit, boolean exist) {
//        if (!exist) {
//            return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
//        }
//        redisTemplate.opsForValue().set(key, value, timeout, unit);
//        return true;
//    }
//
//    @Override
//    public <T> T evalScript(String script, List<String> keys, List<String> args, Class<T> clazz) {
//        SwakScript<T> redisScript = scriptLoad(script, clazz);
//        return evalScript(redisScript, keys, args);
//    }
//
//    @Override
//    public <T> T evalScript(SwakScript<T> script, List<String> keys, List<String> args) {
//        return redisTemplate.execute(getRedisScript(script), keys, args);
//    }
//
//    @Override
//    public <T> SwakScript<T> scriptLoad(String script, Class<T> clazz) {
//        SwakScript<T> swakScript = SwakScript.of(script,clazz);
//        getRedisScript(swakScript);
//        return swakScript;
//    }
//
//    private <T> RedisScript<T> getRedisScript(SwakScript<T> script){
//        RedisScript<T> redisScript = SCRIPT_CACHE.getIfPresent(script.getUniqueCode());
//        if(Objects.nonNull(redisScript)){
//            return redisScript;
//        }
//        DefaultRedisScript<T> defaultRedisScript = new DefaultRedisScript<>();
//        defaultRedisScript.setScriptSource(script.getScriptSource());
//        defaultRedisScript.setResultType(script.getResultType());
//        defaultRedisScript.afterPropertiesSet();
//        SCRIPT_CACHE.put(script.getUniqueCode(),redisScript);
//        return defaultRedisScript;
//    }
//
//    @Override
//    public Set<String> zRevRange(String key, long begin, long end) {
//        Set<Object> range = redisTemplate.opsForZSet().reverseRange(key, begin, end);
//        if (CollectionUtils.isNotEmpty(range)) {
//            return range.stream().map(Object::toString).collect(Collectors.toSet());
//        }
//        return Collections.emptySet();
//    }
//
//    @Override
//    public Boolean zAdd(String key, double score, String value) {
//        return redisTemplate.opsForZSet().add(key, value, score);
//    }
//
//    @Override
//    public Set<String> getKeys(String pattern) {
//        return redisTemplate.keys(pattern);
//    }
//
//
//    @Override
//    public Long hIncrBy(String key, String field, long value) {
//        return redisTemplate.opsForHash().increment(key, field, value);
//    }
//
//    @Override
//    public <T> T hGetObject(String key, String field) {
//        return (T) redisTemplate.opsForHash().get(key, field);
//    }
//
//    @Override
//    public Boolean hSet(String key, String hKey, Object value) {
//        redisTemplate.opsForHash().put(key, hKey, value);
//        return true;
//    }
//
//    @Override
//    public void hMSet(String key, Map<String, String> hashes) {
//        redisTemplate.opsForHash().putAll(key, hashes);
//    }
//
//    @Override
//    public boolean hSet(String key, String hKey, Object value, long timeout, TimeUnit timeUnit) {
//        redisTemplate.executePipelined(new SessionCallback<Void>() {
//            @Override
//            public Void execute(RedisOperations operations) throws DataAccessException {
//                operations.opsForHash().put(key, hKey, value);
//                operations.expire(key, timeout, timeUnit);
//                return null;
//            }
//        });
//        return true;
//    }
//
//    @Override
//    public Long hDel(String key, String field) {
//        return redisTemplate.opsForHash().delete(key, field);
//    }
//
//    @Override
//    public Long hDel(String key, Set<String> fields) {
//        return redisTemplate.opsForHash().delete(key, fields.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
//    }
//
//    @Override
//    public <T> Map<String, T> hGetAllObject(String key) {
//        Map<String, T> resultMap = Maps.newHashMap();
//        Map<Object, Object> entriesMap = redisTemplate.opsForHash().entries(key);
//        if (MapUtils.isNotEmpty(entriesMap)) {
//            entriesMap.forEach((k, v) -> resultMap.put(k.toString(), (T) v));
//        }
//        return resultMap;
//    }
//
//    @Override
//    public Map<String, String> hGetAll(String key, Function<String, Map<String, String>> function) {
//        Map<String, String> result = hGetAllObject(key);
//        if (Objects.nonNull(function)) {
//            if (MapUtils.isEmpty(result)) {
//                result = function.apply(key);
//                if (MapUtils.isNotEmpty(result)) {
//                    this.hMSet(key, result);
//                }
//            }
//        }
//        return result;
//    }
//
//    @Override
//    public Set<String> sMembers(String key) {
//        Set<Object> result = redisTemplate.opsForSet().members(key);
//        if (CollectionUtils.isNotEmpty(result)) {
//            return result.stream().map(Object::toString).collect(Collectors.toSet());
//        }
//        return Collections.emptySet();
//    }
//
//    @Override
//    public Long ttl(String key) {
//        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
//    }
//
//    @Override
//    public void afterPropertiesSet() {
//        SwakAssert.notNull(redisTemplate, "RedisTemplate cannot be null");
//    }
//}
