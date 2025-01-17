package com.swak.jdbc.annotation;

/**
 * FieldStrategy.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public enum FieldStrategy {
    /**
     * 忽略判断
     */
    IGNORED,
    /**
     * 非NULL判断
     */
    NOT_NULL,

    DEFAULT,
    /**
     * 不加入 SQL
     */
    NEVER
}
