package com.swak.cache.annotation;

import com.swak.common.dto.base.BaseOperation;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * {@link ExtCacheable}中的信息的容器类
 * <p>
 * P.S. 未作说明的字段， 其语意去ExtCacheable中见对应的字段说明即可
 *
 * ClassName: ExtCacheableOperation.java
 * 
 * @author colley.ma
 * @since  2021/9/13 11:41:44
 */
@SuppressWarnings("serial")
public class ExtCacheOperation implements BaseOperation {

	private String name;

	/** 当前对象对应的ExtCacheable所处的类 */
	private Class<?> clazz;

	/** 当前对象对应的ExtCacheable所处的方法 */
	private Method method;

	/** expire-time (-1 represent never expire) */
	private int expireTime;

	private TimeUnit timeUnit;

	private boolean multiLevel;

	/**
	 * 对应{@link ExtCacheable#caffeine}中的第一个元素, 如果{@link ExtCacheable#caffeine}不为空的话
	 */
	private CaffeineOperation caffeine;

	private RedisOperation redis;

	private String[] cacheNames;

	private String key;

	private String[] value;

	private String keyGenerator;

	private String cacheManager;

	private String cacheResolver;

	private String condition;

	private String unless;

	private boolean sync;

	public ExtCacheOperation() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public boolean isMultiLevel() {
		return multiLevel;
	}

	public void setMultiLevel(boolean multiLevel) {
		this.multiLevel = multiLevel;
	}

	public CaffeineOperation getCaffeine() {
		return caffeine;
	}

	public void setCaffeine(CaffeineOperation caffeine) {
		this.caffeine = caffeine;
	}

	public String[] getCacheNames() {
		return cacheNames;
	}

	public void setCacheNames(String[] cacheNames) {
		this.cacheNames = cacheNames;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String[] getValue() {
		return value;
	}

	public void setValue(String[] value) {
		this.value = value;
	}

	public String getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(String keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	public String getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(String cacheManager) {
		this.cacheManager = cacheManager;
	}

	public String getCacheResolver() {
		return cacheResolver;
	}

	public void setCacheResolver(String cacheResolver) {
		this.cacheResolver = cacheResolver;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getUnless() {
		return unless;
	}

	public void setUnless(String unless) {
		this.unless = unless;
	}

	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public RedisOperation getRedis() {
		return redis;
	}

	public void setRedis(RedisOperation redis) {
		this.redis = redis;
	}

	public static class Builder {
		private String name;
		private Class<?> clazz;
		private Method method;
		private int expireTime;
		private TimeUnit timeUnit;
		private boolean multiLevel;
		private CaffeineOperation caffeine;
		private RedisOperation redis;
		private String[] cacheNames;
		private String key;
		private String[] value;
		private String keyGenerator;
		private String cacheManager;
		private String cacheResolver;
		private String condition;
		private String unless;
		private boolean sync;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder clazz(Class<?> clazz) {
			this.clazz = clazz;
			return this;
		}

		public Builder method(Method method) {
			this.method = method;
			return this;
		}

		public Builder expireTime(int expireTime) {
			this.expireTime = expireTime;
			return this;
		}

		public Builder timeUnit(TimeUnit timeUnit) {
			this.timeUnit = timeUnit;
			return this;
		}

		public Builder multiLevel(boolean multiLevel) {
			this.multiLevel = multiLevel;
			return this;
		}

		public Builder caffeine(CaffeineOperation caffeine) {
			this.caffeine = caffeine;
			return this;
		}

		public Builder redis(RedisOperation redis) {
			this.redis = redis;
			return this;
		}

		public Builder cacheNames(String[] cacheNames) {
			this.cacheNames = cacheNames;
			return this;
		}

		public Builder key(String key) {
			this.key = key;
			return this;
		}

		public Builder value(String[] value) {
			this.value = value;
			return this;
		}

		public Builder keyGenerator(String keyGenerator) {
			this.keyGenerator = keyGenerator;
			return this;
		}

		public Builder cacheManager(String cacheManager) {
			this.cacheManager = cacheManager;
			return this;
		}

		public Builder cacheResolver(String cacheResolver) {
			this.cacheResolver = cacheResolver;
			return this;
		}

		public Builder condition(String condition) {
			this.condition = condition;
			return this;
		}

		public Builder unless(String unless) {
			this.unless = unless;
			return this;
		}

		public Builder sync(boolean sync) {
			this.sync = sync;
			return this;
		}

		public ExtCacheOperation build() {
			return new ExtCacheOperation(this);
		}
	}

	private ExtCacheOperation(Builder builder) {
		this.name = builder.name;
		this.clazz = builder.clazz;
		this.method = builder.method;
		this.expireTime = builder.expireTime;
		this.timeUnit = builder.timeUnit;
		this.multiLevel = builder.multiLevel;
		this.caffeine = builder.caffeine;
		this.redis = builder.redis;
		this.cacheNames = builder.cacheNames;
		this.key = builder.key;
		this.value = builder.value;
		this.keyGenerator = builder.keyGenerator;
		this.cacheManager = builder.cacheManager;
		this.cacheResolver = builder.cacheResolver;
		this.condition = builder.condition;
		this.unless = builder.unless;
		this.sync = builder.sync;
	}
}
