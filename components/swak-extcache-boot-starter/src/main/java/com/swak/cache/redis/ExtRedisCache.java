package com.swak.cache.redis;

import com.swak.core.cache.DistributedCacheProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ExtRedisCache extends AbstractValueAdaptingCache {
	private Logger logger = LoggerFactory.getLogger(ExtRedisCache.class);

	private final String name;

	private final DistributedCacheProxy cache;

	/**
	 * TimeUnit is SECONDS
	 */
	private long entryTimeout = -1;

	public ExtRedisCache(String name, DistributedCacheProxy cache) {
		this(name, cache, -1, true);
	}

	public ExtRedisCache(String name, DistributedCacheProxy cache, long entryTimeout) {
		this(name, cache, entryTimeout, true);
	}

	public ExtRedisCache(String name, DistributedCacheProxy cache, long entryTimeout, boolean allowNullValues) {
		super(allowNullValues);
		Assert.notNull(cache, "RedisCacheProxy is required; it must not be null");
		Assert.notNull(name, "name is required; it must not be null");
		this.name = name;
		this.cache = cache;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DistributedCacheProxy getNativeCache() {
		return cache;
	}

	@Override
	protected Object lookup(Object key) {
		final String lastKey = String.valueOf(key);
		return cache.getObject(lastKey);
	}

	@Override
	public void evict(final Object key) {
		final String lastKey = String.valueOf(key);
		if (logger.isDebugEnabled()) {
			logger.debug("Removing from cache, key " + (lastKey.endsWith("*") ? "pattern:" : ":") + lastKey);
		}
		cache.delete(lastKey);
	}

	@Override
	public void clear() {
	}

	public void setEntryTimeout(Integer entryTimeout) {
		this.entryTimeout = entryTimeout;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		T oldValue = (T) lookup(key);
		if (oldValue == null) {
			oldValue = (T) new LoadFunction(valueLoader).apply(key);
		}
		return (T) fromStoreValue(oldValue);
	}

	@Override
	public void put(Object key, Object value) {
		final String lastKey = String.valueOf(key);
		if (entryTimeout > 0) {
			cache.setObject(lastKey, value, entryTimeout, TimeUnit.SECONDS);
		} else {
			cache.setObject(lastKey, value);
		}
	}

	private class LoadFunction implements Function<Object, Object> {

		private final Callable<?> valueLoader;

		public LoadFunction(Callable<?> valueLoader) {
			this.valueLoader = valueLoader;
		}

		@Override
		public Object apply(Object o) {
			try {
				return toStoreValue(this.valueLoader.call());
			} catch (Exception ex) {
				throw new ValueRetrievalException(o, this.valueLoader, ex);
			}
		}
	}
}
