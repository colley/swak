
package com.swak.cache.caffeine;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.swak.cache.ExtCacheRepository;
import com.swak.cache.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class ExtCaffeineCacheManager extends AbstractCacheManager {

	private ExtCacheRepository cacheRepository;

	private boolean dynamic = true;

	private boolean allowNullValues = true;

	private Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();

	@Nullable
	private CacheLoader<Object, Object> cacheLoader;
	
	private ExtCaffeineCacheManager() {}
	

	@Override
	protected Collection<? extends Cache> loadCaches() {
		Collection<Cache> caches = Collections.emptySet();
		if (cacheRepository == null) {
			return caches;
		}
		Map<String, CaffeineOperation> caffeineOperation = cacheRepository.loadCaffeineOop();
		if (MapUtils.isEmpty(caffeineOperation)) {
			return caches;
		}
		caffeineOperation.forEach((key, value) -> caches.add(createCaffeineCache(key, value)));
		return caches;
	}

	private Cache createCaffeineCache(String name, CaffeineOperation oop) {
		return adaptCaffeineCache(name, createNativeCaffeineCacheByOop(oop));
	}

	protected Cache adaptCaffeineCache(String name, com.github.benmanes.caffeine.cache.Cache<Object, Object> cache) {
		return new CaffeineCache(name, cache, isAllowNullValues());
	}

	private com.github.benmanes.caffeine.cache.Cache<Object, Object> createNativeCaffeineCacheByOop(
			CaffeineOperation caffeineOop) {
		Caffeine<Object, Object> instance = Caffeine.newBuilder();
		if (caffeineOop == null) {
			return instance.build();
		}
		// 初始化容量
		instance.initialCapacity(caffeineOop.getInitialCapacity());
		// 淘汰机制
		long maximumSize = caffeineOop.getMaximumSize();
		if (maximumSize > 0) {
			instance.maximumSize(maximumSize);
		} else {
			instance.maximumWeight(caffeineOop.getMaximumWeight());
		}
		// 刷新机制
		int refreshAfterWrite = caffeineOop.getRefreshAfterWrite();
		if (refreshAfterWrite > 0) {
			instance.refreshAfterWrite(Duration.of(refreshAfterWrite, caffeineOop.getTimeUnit4Refresh()));
		}
		// 统计(命中率等)信息
		if (caffeineOop.isRecordStats()) {
			instance.recordStats();
		}
		// 过期时间
		Duration duration = Duration.of(caffeineOop.getExpireTime(), caffeineOop.getTimeUnit());
		CaffeineExpireStrategy expireStrategy = caffeineOop.getExpireStrategy();
		if (expireStrategy == CaffeineExpireStrategy.EXPIRE_AFTER_WRITE) {
			instance.expireAfterWrite(duration);
		} else if (expireStrategy == CaffeineExpireStrategy.EXPIRE_AFTER_ACCESS) {
			instance.expireAfterAccess(duration);
		} else {
			throw new IllegalArgumentException(
					"[Swak-Cache] cannot discern CaffeineExpireStrategyEnum enum-item [" + expireStrategy + "]");
		}
		// key是否采用虚引用
		if (caffeineOop.getKeyQuoteType() == CaffeineKeyQuoteType.WEAK) {
			instance.weakKeys();
		}
		// value是否采用虚引用/软引用
		CaffeineValueQuoteType valueQuoteType = caffeineOop.getValueQuoteType();
		if (valueQuoteType == CaffeineValueQuoteType.WEAK) {
			instance.weakValues();
		} else if (valueQuoteType == CaffeineValueQuoteType.SOFT) {
			instance.softValues();
		} else {
			// default
			log.debug("[Swak-Cache] CaffeineValueQuoteType used -> {}", valueQuoteType);
		}
		return instance.build();
	}

	/**
	 * Specify whether to accept and convert {@code null} values for all caches in
	 * this cache manager.
	 * <p>
	 * Default is "true", despite Caffeine itself not supporting {@code null}
	 * values. An internal holder object will be used to store user-level
	 * {@code null}s.
	 */
	public void setAllowNullValues(boolean allowNullValues) {
		if (this.allowNullValues != allowNullValues) {
			this.allowNullValues = allowNullValues;
		}
	}

	/**
	 * Return whether this cache manager accepts and converts {@code null} values
	 * for all of its caches.
	 */
	public boolean isAllowNullValues() {
		return this.allowNullValues;
	}

	/**
	 * Build a common {@link CaffeineCache} instance for the specified cache name,
	 * using the common Caffeine configuration specified on this cache manager.
	 * <p>
	 * Delegates to {@link #adaptCaffeineCache} as the adaptation method to Spring's
	 * cache abstraction (allowing for centralized decoration etc), passing in a
	 * freshly built native Caffeine Cache instance.
	 * 
	 * @param name the name of the cache
	 * @return the Spring CaffeineCache adapter (or a decorator thereof)
	 * @see #adaptCaffeineCache
	 * @see #createNativeCaffeineCache
	 */
	protected Cache createCaffeineCache(String name) {
		return adaptCaffeineCache(name, createNativeCaffeineCache(name));
	}

	/**
	 * Build a common Caffeine Cache instance for the specified cache name, using
	 * the common Caffeine configuration specified on this cache manager.
	 * 
	 * @param name the name of the cache
	 * @return the native Caffeine Cache instance
	 * @see #createCaffeineCache
	 */
	protected com.github.benmanes.caffeine.cache.Cache<Object, Object> createNativeCaffeineCache(String name) {
		return (this.cacheLoader != null ? this.cacheBuilder.build(this.cacheLoader) : this.cacheBuilder.build());
	}

	@Nullable
	@Override
	protected Cache getMissingCache(String name) {
		if (cacheRepository == null) {
			return this.dynamic ? createCaffeineCache(name) : null;
		}
		ExtCacheOperation cacheableOop = cacheRepository.determineExtCacheOop(name);
		if (cacheableOop == null || cacheableOop.getCaffeine() == null) {
			return this.dynamic ? createCaffeineCache(name) : null;
		}

		return createCaffeineCache(name, cacheableOop.getCaffeine());
	}

	public static class Builder {
		private ExtCacheRepository cacheRepository;
		private boolean dynamic;
		private boolean allowNullValues;
		private Caffeine<Object, Object> cacheBuilder;
		private CacheLoader<Object, Object> cacheLoader;

		public Builder cacheRepository(ExtCacheRepository cacheRepository) {
			this.cacheRepository = cacheRepository;
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

		public Builder cacheBuilder(Caffeine<Object, Object> cacheBuilder) {
			this.cacheBuilder = cacheBuilder;
			return this;
		}

		public Builder cacheLoader(CacheLoader<Object, Object> cacheLoader) {
			this.cacheLoader = cacheLoader;
			return this;
		}

		public ExtCaffeineCacheManager build() {
			return new ExtCaffeineCacheManager(this);
		}
	}

	private ExtCaffeineCacheManager(Builder builder) {
		this.cacheRepository = builder.cacheRepository;
		this.dynamic = builder.dynamic;
		this.allowNullValues = builder.allowNullValues;
		this.cacheBuilder = builder.cacheBuilder;
		this.cacheLoader = builder.cacheLoader;
	}
}