
package com.swak.cache.annotation;

public enum CaffeineExpireStrategy {

	/**
	 * 最后一次写入(或访问)后经过固定时间过期
	 */
	EXPIRE_AFTER_ACCESS,

	/**
	 * 最后一次写入后经过固定时间过期
	 */
	EXPIRE_AFTER_WRITE
}
