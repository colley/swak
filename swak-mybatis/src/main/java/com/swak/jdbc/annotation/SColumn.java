package com.swak.jdbc.annotation;

import java.lang.annotation.*;

/**
 * TableColumn.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface SColumn {

    String value() default "";
    /**
     * 是否为数据库表字段
     * 默认 true 存在，false 不存在
     */
    boolean exist() default true;

    FieldStrategy insertStrategy() default FieldStrategy.DEFAULT;

    FieldStrategy updateStrategy() default FieldStrategy.DEFAULT;
}
