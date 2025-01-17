package com.swak.formula.annotation;

import java.lang.annotation.*;
/**
 * FormulaNamed.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormulaNamed {
    /**
     * 函数名
     */
    String value();


    /**
     * 函数描述
     */
    String desc() default "";
}
