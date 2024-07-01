package com.swak.core.cache;


import com.swak.core.script.SwakScript;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public interface DistributedCacheProxy {

    <T> T getObject(final String key);

    <T> boolean setObject(String key, T value, long timeout, TimeUnit unit);

    <T> boolean setObject(String key, T value);

    Boolean exists(final String key);

    Boolean expire(final String key, long timeout, TimeUnit unit);

    Boolean hExists(final String key, final String field);

    <T> Boolean sAdd(String key, T value);

    <T> Boolean sIsMember(String key, T value);

    Long sCard(String key);

    Long delete(String... keys);

    Long decrBy(final String key, final long delta);

    Long incr(final String key);

    Long incrBy(final String key, final long delta);

    Boolean set(final String key, final String value, long timeout, TimeUnit unit,
                       final boolean exist);

     <T> T evalScript(final String script, final List<String> keys, final List<String> args,Class<T> clazz);

    <T> T evalScript(final SwakScript<T> script, final List<String> keys, final List<String> args);

    <T> SwakScript<T> scriptLoad(String script, Class<T> clazz) throws Exception;

     Set<String> zRevRange(final String key, final long begin, final long end);

     Boolean zAdd(final String key, final double score, final String value);


    Set<String> getKeys(String pattern);

    Long hIncrBy(String key, String field, long value);

    <T> T hGetObject(String key, String field);

    Boolean hSet(String key, String hKey, Object value);

    void hMSet(String key, Map<String, String> hashes);

     boolean hSet(String key, String hKey, Object value, long timeout, TimeUnit timeUnit);

     Long hDel(String key, String field);

     Long hDel(String key, Set<String> fields);

    <T> Map<String, T> hGetAllObject(String key);

    Map<String, String> hGetAll(String key, Function<String, Map<String, String>> function);

    Set<String> sMembers(String key);

    Long ttl(String key);
}
