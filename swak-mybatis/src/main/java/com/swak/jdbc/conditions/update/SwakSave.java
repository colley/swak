package com.swak.jdbc.conditions.update;

import java.util.Map;

/**
 * SwakInsert.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface SwakSave<Children, R,T>{

    default Children addColumn(R column, Object val) {
        return addColumn(true, column, val);
    }

     Children addColumn(R ... columns);

    Children addValue(R column,Object value);

    Children addColumn(Map<R,Object> columnValue);

    Children from(String tableName);

    Children setEntity(T entity);
    /**
     * 设置 SqlInsert
     */
    Children addColumn(boolean condition, R column, Object val);

    /**
     * 获取 SqlInsert
     */
    String getSqlSet();
}
