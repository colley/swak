
package com.swak.cache;

import com.swak.cache.annotation.CaffeineOperation;
import com.swak.cache.annotation.ExtCacheOperation;
import com.swak.cache.annotation.RedisOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultExtCacheRepository implements ExtCacheRepository {


	private static final Map<String, ExtCacheOperation> extCacheableRepository = new ConcurrentHashMap<>();

	private static final Map<String, CaffeineOperation> caffeineRepository = new ConcurrentHashMap<>();
	
	private static final Map<String, RedisOperation> redisRepository = new ConcurrentHashMap<>();

	@Override
	public ExtCacheOperation determineExtCacheOop(String name) {
		return MapUtils.isNotEmpty(extCacheableRepository)?
				extCacheableRepository.get(name):null;
	}

	@Override
	public Map<String, ExtCacheOperation> loadExtCacheableOop() {
		return extCacheableRepository;
	}

	@Override
	public Map<String, CaffeineOperation> loadCaffeineOop() {
		return caffeineRepository;
	}

	private static void doRegistration(ExtCacheOperation operation) {
		extCacheableRepository.putIfAbsent(operation.getCacheNames()[0], operation);
		//Caffeine不为空
		if (operation.getCaffeine() != null) {
			caffeineRepository.computeIfAbsent(operation.getCacheNames()[0], cacheName -> operation.getCaffeine());
		}
		//Redis不为空
		if(operation.getRedis()!=null) {
			redisRepository.computeIfAbsent(operation.getCacheNames()[0], cacheName -> operation.getRedis());
		}
	}

	public static void doRegistration(Collection<ExtCacheOperation> operation) {
		if (CollectionUtils.isEmpty(operation)) {
			return;
		}
		log.info("[Swak-Cache] - Found ExtCacheable ,size:{}", operation.size());
		operation.forEach(DefaultExtCacheRepository::doRegistration);
	}

	@Override
	public Map<String, RedisOperation> loadExtRedisOop() {
		return redisRepository;
	}
}
