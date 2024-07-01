package com.swak.cache.annotation;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 	扩展{@link Cacheable}
 */
@Cacheable
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtCacheable {
	
	/** expire-time (-1 represent never expire) */
    int expireTime() default -1;
    
    /** unit for expireTime */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    
	/** caffeine-cache (make sure size &lt;= 1) */
	Caffeine[] caffeine() default {};
	
	/** redis-cache (make sure size &lt;= 1) */
	Redis[] redis() default {};
	
	/** isLocal (true 默认使用ehcache,指定caffeine 使用 caffeine) */
	boolean multiLevel() default false;

	/** @see Cacheable#cacheNames() */
	@AliasFor(annotation = Cacheable.class)
	String[] cacheNames() default {};

	/** @see Cacheable#key() */
	@AliasFor(annotation = Cacheable.class)
	String key();

	/** @see Cacheable#value() */
	@AliasFor(annotation = Cacheable.class)
	String[] value() default {};

	/** @see Cacheable#keyGenerator() */
	@AliasFor(annotation = Cacheable.class)
	String keyGenerator() default "";

	/** @see Cacheable#cacheManager() */
	@AliasFor(annotation = Cacheable.class)
	String cacheManager() default "cacheManager";

	/** @see Cacheable#cacheResolver() */
	@AliasFor(annotation = Cacheable.class)
	String cacheResolver() default "";

	/** @see Cacheable#condition() */
	@AliasFor(annotation = Cacheable.class)
	String condition() default "";

	/** @see Cacheable#unless() */
	@AliasFor(annotation = Cacheable.class)
	String unless() default "";

	/** @see Cacheable#sync() */
	@AliasFor(annotation = Cacheable.class)
	boolean sync() default false;
}
