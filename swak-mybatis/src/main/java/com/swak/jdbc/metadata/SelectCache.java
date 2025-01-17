package com.swak.jdbc.metadata;

import lombok.Getter;

/**
 * 缓存
 */
@Getter
public class SelectCache {

    /**
     * 实体类
     */
    private final Class<?> clazz;

    /**
     * 查询字段
     */
    private final String column;

    /**
     * 字段类型
     */
    private final Class<?> columnType;


    /**
     * 字段属性名
     */
    private final String columProperty;

    /**
     * mp 字段信息
     */
    private final TableFieldInfo tableFieldInfo;


    public SelectCache(Class<?> clazz, String column, Class<?> columnType, String columProperty, TableFieldInfo tableFieldInfo) {
        this.clazz = clazz;
        this.column = column;
        this.columnType = columnType;
        this.columProperty = columProperty;
        this.tableFieldInfo = tableFieldInfo;
    }
}
