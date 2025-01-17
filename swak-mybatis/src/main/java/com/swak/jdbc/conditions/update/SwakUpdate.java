package com.swak.jdbc.conditions.update;

/**
 * SwakUpdate.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface SwakUpdate<Children, R,T> {


    /**
     * ignore
     */
    default Children set(R column, Object val) {
        return set(true, column, val);
    }

    Children from(String tableName);

    /**
     * 设置 更新 SQL 的 SET 片段
     */
    Children set(boolean condition, R column, Object val);

    Children setEntity(T entity);
    /**
     * ignore
     */
    default Children setSql(String sql) {
        return setSql(true, sql);
    }

    /**
     * 设置 更新 SQL 的 SET 片段
     *
     * @param sql set sql
     * @return children
     */
    Children setSql(boolean condition, String sql);

    /**
     * 获取 更新 SQL 的 SET 片段
     */
    String getSqlSet();
}
