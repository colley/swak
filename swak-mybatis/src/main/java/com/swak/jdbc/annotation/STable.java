package com.swak.jdbc.annotation;

import java.lang.annotation.*;

/**
 * TableNamed.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface STable {

    /**
     * 实体对应的表名
     */
    String value() default "";

}
