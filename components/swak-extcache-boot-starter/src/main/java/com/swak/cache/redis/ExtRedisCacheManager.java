package com.swak.cache.redis;

import com.swak.cache.ExtCacheRepository;
import com.swak.cache.annotation.ExtCacheOperation;
import com.swak.cache.annotation.RedisOperation;
import com.swak.core.cache.DistributedCacheProxy;
import org.apache.commons.collections4.MapUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


public class ExtRedisCacheManager extends AbstractCacheManager {

	private Collection<ExtRedisCache> caches = Collections.emptySet();

	private ExtCacheRepository cacheRepository;

	private DistributedCacheProxy customCache;

	private ExtRedisCacheManager() {
	}

	private synchronized void loadCachesConfig() {
		if (cacheRepository == null) {
			return;
		}
		Map<String, RedisOperation> cacheOperation = cacheRepository.loadExtRedisOop();
		if (!MapUtils.isNotEmpty(cacheOperation)) {
			return;
		}
		cacheOperation.forEach((key, value) -> {
			caches.add(createRedisCache(key, value));
		});
	}

	@Override
	protected Cache getMissingCache(String name) {
		ExtCacheOperation oop = cacheRepository.determineExtCacheOop(name);
		//获取总配置的时间
		if(oop!=null) {
			return createRedisCache(name, oop);
		}
		return new ExtRedisCache(name, customCache);
	}

	private ExtRedisCache createRedisCache(String name, ExtCacheOperation oop) {
		long entryTimeout = oop.getExpireTime();
		if (entryTimeout > 0) {
			entryTimeout = oop.getTimeUnit().toSeconds(entryTimeout);
		}
		return new ExtRedisCache(name, customCache, entryTimeout);
	}
	
	private ExtRedisCache createRedisCache(String name, RedisOperation oop) {
		long entryTimeout = oop.getExpireTime();
		if (entryTimeout > 0) {
			entryTimeout = oop.getTimeUnit().toSeconds(entryTimeout);
		}
		return new ExtRedisCache(name, customCache, entryTimeout);
	}

	@Override
	protected Collection<? extends Cache> loadCaches() {
		loadCachesConfig();
		return caches;
	}

	public static class Builder {
		private ExtCacheRepository cacheRepository;
		private DistributedCacheProxy customCache;

		public Builder cacheRepository(ExtCacheRepository cacheRepository) {
			this.cacheRepository = cacheRepository;
			return this;
		}

		public Builder customCache(DistributedCacheProxy customCache) {
			this.customCache = customCache;
			return this;
		}

		public ExtRedisCacheManager build() {
			return new ExtRedisCacheManager(this);
		}
	}

	private ExtRedisCacheManager(Builder builder) {
		this.cacheRepository = builder.cacheRepository;
		this.customCache = builder.customCache;
	}
}
