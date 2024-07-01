package com.swak.lock.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * lock
 * @author colley.ma
 * @since 3.0.0
 **/
@Documented
@Target({java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lockable {

    /**
     * timeToTry
     **/
    long timeToTry() default -1;

    /**
     * leaseTime
     */
    long leaseTime() default -1;

    /** unit for expireTime */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    String key() default "";

    String fallbackMethod() default "";
}
