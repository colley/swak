
package com.swak.cache.manager;

import com.google.common.collect.Sets;
import com.swak.cache.ExtCacheRepository;
import com.swak.cache.annotation.ExtCacheOperation;
import com.swak.cache.caffeine.ExtCaffeineCacheManager;
import com.swak.cache.redis.ExtRedisCacheManager;
import com.swak.core.cache.DistributedCacheProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ExtCacheManager extends AbstractCacheManager {

	private ExtCacheRepository cacheRepository;

	private DistributedCacheProxy redisCacheProxy;

	private boolean dynamic = true;

	private boolean allowNullValues = true;

	private CacheManager defaultCacheManager;

	private CacheManager extCaffeineCacheManager = null;

	private CacheManager extRedisCacheManager = null;

	public ExtCacheManager(CacheManager defaultCacheManager) {
		this.defaultCacheManager = defaultCacheManager;
	}

	@Override
	public void afterPropertiesSet() {
		this.extCaffeineCacheManager = new ExtCaffeineCacheManager.Builder().allowNullValues(allowNullValues)
				.dynamic(dynamic).cacheRepository(cacheRepository).build();
		this.extRedisCacheManager = new ExtRedisCacheManager.Builder().cacheRepository(cacheRepository)
				.customCache(redisCacheProxy).build();
		log.info(" found defaultCacheManager [{}]", defaultCacheManager);
		initializeCaches();
	}

	@Override
	protected Collection<? extends Cache> loadCaches() {
		Collection<Cache> caches = Sets.newHashSet();
		if (cacheRepository == null) {
			return caches;
		}
		Map<String, ExtCacheOperation> caffeineOperation = cacheRepository.loadExtCacheableOop();
		if (MapUtils.isEmpty(caffeineOperation)) {
			return caches;
		}
		caffeineOperation.forEach((key, value) -> {
			caches.add(createExtCache(key, value));
		});
		return caches;
	}

	private Cache createExtCache(String name, ExtCacheOperation oop) {
		if (!oop.isMultiLevel()) {
			//如果同时设置了redis和Caffeine 默认支持多级缓存
			if(oop.getRedis()!=null && 
					oop.getCaffeine() != null) {
				new MultiLevelCache(extCaffeineCacheManager.getCache(name),
						extRedisCacheManager.getCache(name));
			}
			//Caffeine 不为空使用Caffeine缓存
			if (oop.getCaffeine() != null) {
				return extCaffeineCacheManager.getCache(name);
			}
			//Redis 不为空使用Redis缓存
			if(oop.getRedis()!=null) {
				return extRedisCacheManager.getCache(name);
			}
			// 默认
			return defaultCacheManager.getCache(name);
		}
		
		Cache firstCache = Optional.ofNullable(oop.getCaffeine() != null ? 
				extCaffeineCacheManager.getCache(name) : null)
				.orElse(defaultCacheManager.getCache(name));
		
		Cache secondCache = extRedisCacheManager.getCache(name);
		
		return new MultiLevelCache(firstCache, secondCache);
	}

	@Nullable
	@Override
	protected Cache getMissingCache(String name) {
		ExtCacheOperation oop = cacheRepository.determineExtCacheOop(name);
		if(oop!=null) {
			return createExtCache(name, oop);
		}
		return defaultCacheManager.getCache(name);
	}

	public void setAllowNullValues(boolean allowNullValues) {
		if (this.allowNullValues != allowNullValues) {
			this.allowNullValues = allowNullValues;
		}
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public static class Builder {
		private ExtCacheRepository cacheRepository;
		private DistributedCacheProxy redisCacheProxy;
		private boolean dynamic;
		private boolean allowNullValues;
		private CacheManager defaultCacheManager;

		public Builder cacheRepository(ExtCacheRepository cacheRepository) {
			this.cacheRepository = cacheRepository;
			return this;
		}

		public Builder redisCacheProxy(DistributedCacheProxy redisCacheProxy) {
			this.redisCacheProxy = redisCacheProxy;
			return this;
		}

		public Builder dynamic(boolean dynamic) {
			this.dynamic = dynamic;
			return this;
		}

		public Builder allowNullValues(boolean allowNullValues) {
			this.allowNullValues = allowNullValues;
			return this;
		}

		public Builder defaultCacheManager(CacheManager defaultCacheManager) {
			this.defaultCacheManager = defaultCacheManager;
			return this;
		}

		public ExtCacheManager build() {
			return new ExtCacheManager(this);
		}
	}

	private ExtCacheManager(Builder builder) {
		this.cacheRepository = builder.cacheRepository;
		this.redisCacheProxy = builder.redisCacheProxy;
		this.dynamic = builder.dynamic;
		this.allowNullValues = builder.allowNullValues;
		this.defaultCacheManager = builder.defaultCacheManager;
	}
}
