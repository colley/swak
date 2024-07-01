package com.swak.cache.annotation;

import java.time.temporal.ChronoUnit;

/**
 * {@link Caffeine}中的信息的容器类
 * P.S. 每个字段的语意见Caffeine中对应的字段即可
 * ClassName: CaffeineOperation.java 
 * @author colley.ma
 * @since  2021年9月13日 上午11:41:06
 */
public class CaffeineOperation {


	private long maximumSize;

	private long maximumWeight;

	private String weigher4MaximumWeight;

	private int initialCapacity;

	private int refreshAfterWrite;

	private ChronoUnit timeUnit4Refresh;

	private String cacheLoader4Refresh;

	private boolean recordStats;

	private CaffeineKeyQuoteType keyQuoteType;

	private CaffeineValueQuoteType valueQuoteType;

	private int expireTime;

	private ChronoUnit timeUnit;

	private CaffeineExpireStrategy expireStrategy;

	public long getMaximumSize() {
		return maximumSize;
	}

	public void setMaximumSize(long maximumSize) {
		this.maximumSize = maximumSize;
	}

	public long getMaximumWeight() {
		return maximumWeight;
	}

	public void setMaximumWeight(long maximumWeight) {
		this.maximumWeight = maximumWeight;
	}

	public String getWeigher4MaximumWeight() {
		return weigher4MaximumWeight;
	}

	public void setWeigher4MaximumWeight(String weigher4MaximumWeight) {
		this.weigher4MaximumWeight = weigher4MaximumWeight;
	}

	public int getInitialCapacity() {
		return initialCapacity;
	}

	public void setInitialCapacity(int initialCapacity) {
		this.initialCapacity = initialCapacity;
	}

	public int getRefreshAfterWrite() {
		return refreshAfterWrite;
	}

	public void setRefreshAfterWrite(int refreshAfterWrite) {
		this.refreshAfterWrite = refreshAfterWrite;
	}

	public ChronoUnit getTimeUnit4Refresh() {
		return timeUnit4Refresh;
	}

	public void setTimeUnit4Refresh(ChronoUnit timeUnit4Refresh) {
		this.timeUnit4Refresh = timeUnit4Refresh;
	}

	public String getCacheLoader4Refresh() {
		return cacheLoader4Refresh;
	}

	public void setCacheLoader4Refresh(String cacheLoader4Refresh) {
		this.cacheLoader4Refresh = cacheLoader4Refresh;
	}

	public boolean isRecordStats() {
		return recordStats;
	}

	public void setRecordStats(boolean recordStats) {
		this.recordStats = recordStats;
	}

	public CaffeineKeyQuoteType getKeyQuoteType() {
		return keyQuoteType;
	}

	public void setKeyQuoteType(CaffeineKeyQuoteType keyQuoteType) {
		this.keyQuoteType = keyQuoteType;
	}

	public CaffeineValueQuoteType getValueQuoteType() {
		return valueQuoteType;
	}

	public void setValueQuoteType(CaffeineValueQuoteType valueQuoteType) {
		this.valueQuoteType = valueQuoteType;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public ChronoUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(ChronoUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public CaffeineExpireStrategy getExpireStrategy() {
		return expireStrategy;
	}

	public void setExpireStrategy(CaffeineExpireStrategy expireStrategy) {
		this.expireStrategy = expireStrategy;
	}

	public static class Builder {
		private long maximumSize;
		private long maximumWeight;
		private String weigher4MaximumWeight;
		private int initialCapacity;
		private int refreshAfterWrite;
		private ChronoUnit timeUnit4Refresh;
		private String cacheLoader4Refresh;
		private boolean recordStats;
		private CaffeineKeyQuoteType keyQuoteType;
		private CaffeineValueQuoteType valueQuoteType;
		private int expireTime;
		private ChronoUnit timeUnit;
		private CaffeineExpireStrategy expireStrategy;

		public Builder maximumSize(long maximumSize) {
			this.maximumSize = maximumSize;
			return this;
		}

		public Builder maximumWeight(long maximumWeight) {
			this.maximumWeight = maximumWeight;
			return this;
		}

		public Builder weigher4MaximumWeight(String weigher4MaximumWeight) {
			this.weigher4MaximumWeight = weigher4MaximumWeight;
			return this;
		}

		public Builder initialCapacity(int initialCapacity) {
			this.initialCapacity = initialCapacity;
			return this;
		}

		public Builder refreshAfterWrite(int refreshAfterWrite) {
			this.refreshAfterWrite = refreshAfterWrite;
			return this;
		}

		public Builder timeUnit4Refresh(ChronoUnit timeUnit4Refresh) {
			this.timeUnit4Refresh = timeUnit4Refresh;
			return this;
		}

		public Builder cacheLoader4Refresh(String cacheLoader4Refresh) {
			this.cacheLoader4Refresh = cacheLoader4Refresh;
			return this;
		}

		public Builder recordStats(boolean recordStats) {
			this.recordStats = recordStats;
			return this;
		}

		public Builder keyQuoteType(CaffeineKeyQuoteType keyQuoteType) {
			this.keyQuoteType = keyQuoteType;
			return this;
		}

		public Builder valueQuoteType(CaffeineValueQuoteType valueQuoteType) {
			this.valueQuoteType = valueQuoteType;
			return this;
		}

		public Builder expireTime(int expireTime) {
			this.expireTime = expireTime;
			return this;
		}

		public Builder timeUnit(ChronoUnit timeUnit) {
			this.timeUnit = timeUnit;
			return this;
		}

		public Builder expireStrategy(CaffeineExpireStrategy expireStrategy) {
			this.expireStrategy = expireStrategy;
			return this;
		}

		public CaffeineOperation build() {
			return new CaffeineOperation(this);
		}
	}

	private CaffeineOperation(Builder builder) {
		this.maximumSize = builder.maximumSize;
		this.maximumWeight = builder.maximumWeight;
		this.weigher4MaximumWeight = builder.weigher4MaximumWeight;
		this.initialCapacity = builder.initialCapacity;
		this.refreshAfterWrite = builder.refreshAfterWrite;
		this.timeUnit4Refresh = builder.timeUnit4Refresh;
		this.cacheLoader4Refresh = builder.cacheLoader4Refresh;
		this.recordStats = builder.recordStats;
		this.keyQuoteType = builder.keyQuoteType;
		this.valueQuoteType = builder.valueQuoteType;
		this.expireTime = builder.expireTime;
		this.timeUnit = builder.timeUnit;
		this.expireStrategy = builder.expireStrategy;
	}
}