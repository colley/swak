package com.swak.jdbc.metadata;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * TableInfo.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Data
public class TableInfo {
    /**
     * 实体类型
     */
    private Class<?> entityType;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 表字段信息列表
     */
    private List<TableFieldInfo> fieldList;


    public TableInfo(Class<?> entityType) {
        this.entityType = entityType;
    }

    public List<TableFieldInfo> getFieldList() {
        return Collections.unmodifiableList(fieldList);
    }
}
