package com.swak.cache.annotation;

import java.util.concurrent.TimeUnit;

/**
 * {@link Redis}中的信息的容器类
 * P.S. 每个字段的语意见Caffeine中对应的字段即可
 */
public class RedisOperation{

	private String useRedisTemplate;

	private int expireTime;

	private TimeUnit timeUnit;

	public String getUseRedisTemplate() {
		return useRedisTemplate;
	}

	public void setUseRedisTemplate(String useRedisTemplate) {
		this.useRedisTemplate = useRedisTemplate;
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

	public static class Builder {
		private String useRedisTemplate;
		private int expireTime;
		private TimeUnit timeUnit;

		public Builder useRedisTemplate(String useRedisTemplate) {
			this.useRedisTemplate = useRedisTemplate;
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

		public RedisOperation build() {
			return new RedisOperation(this);
		}
	}

	private RedisOperation(Builder builder) {
		this.useRedisTemplate = builder.useRedisTemplate;
		this.expireTime = builder.expireTime;
		this.timeUnit = builder.timeUnit;
	}
}