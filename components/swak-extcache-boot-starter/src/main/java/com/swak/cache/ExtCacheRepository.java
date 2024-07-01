
package com.swak.cache;

import com.swak.cache.annotation.CaffeineOperation;
import com.swak.cache.annotation.ExtCacheOperation;
import com.swak.cache.annotation.RedisOperation;

import java.util.Map;

public interface ExtCacheRepository {
	
	 ExtCacheOperation determineExtCacheOop(String name);
	
	 Map<String,ExtCacheOperation> loadExtCacheableOop();
	
	 Map<String,RedisOperation> loadExtRedisOop();
	
	 Map<String,CaffeineOperation> loadCaffeineOop();
}
