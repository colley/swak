/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 19-9-30 上午11:35
 * History:
 */
package com.swak.core.command.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *
 * @ClassName: SwakFallback.java
 * @author: colley.ma
 * @date: 2022/01/20
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwakFallback {

    /** 兜底方法 **/
    abstract String fallbackMethod() default "";

    /** 兜底类型 {@link SwakExecutionType} **/
    abstract SwakExecutionType async() default SwakExecutionType.SYNCHRONOUS;
}
