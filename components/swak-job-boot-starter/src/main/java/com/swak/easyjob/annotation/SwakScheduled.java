/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: SwakJob.java
 * Encoding: UTF-8
 * Date: 2021年4月21日 下午2:20:39
 * History:
 */
package com.swak.easyjob.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * The interface Swak scheduled.
 *
 * @author colley
 */
@Documented
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SwakScheduled {

    /**
     * 支持Spell表达式
     */
    String enabled() default "";

    /**
     * Value string.
     */
    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";

    /**
     * Cron string [ ].
     *
     * @return the string [ ]
     */
    String[] cron() default {};

    /**
     * Fixed rate long [ ].
     *
     * @return the long [ ]
     */
    long[] fixedRate() default {};

    /**
     * Params string [ ].
     *
     * @return the string [ ]
     */
    String[] params() default {};

    /**
     * Unit time unit.
     *
     * @return the time unit
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

    /**
     * Job name string.
     *
     * @return the string
     */
    String jobName() default "";

    /**
     * 是否分布式执行
     *
     * @return boolean
     */
    boolean distributed() default true;

    /**
     * Sharding count int.
     *
     * @return the int
     */
    int shardingCount() default 1;
}
