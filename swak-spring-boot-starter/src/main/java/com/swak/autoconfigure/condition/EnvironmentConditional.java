package com.swak.autoconfigure.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(EnvironmentCondition.class) //<- meta annotation condition
public @interface EnvironmentConditional {
    //环境
    String[] env() default {};
    //是否取反
    boolean reversed() default false;
}
