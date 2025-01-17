package com.swak.jdbc.metadata;

import com.swak.jdbc.annotation.FieldStrategy;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;

/**
 * TableFieldInfo.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Data
@Accessors(chain = true)
public class TableFieldInfo {
    /**
     * 属性
     */
    private  Field field;
    /**
     * 字段名
     */
    private  String column;
    /**
     * 属性名
     */
    private  String property;

    FieldStrategy insertStrategy;

    FieldStrategy updateStrategy;

    private Class<?> columnType;

}
