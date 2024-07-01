package com.swak.jdbc.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 14:24
 */
@Data
@Accessors(chain = true)
public class SqlInfo {

    /**
     * SQL 内容
     */
    private String sql;
    /**
     * 是否排序
     */
    private boolean orderBy;

    /**
     * 使用工厂快速构建 SQLInfo，默认支持排序
     *
     * @param sql SQL 语句
     * @return 返回新的 SQL 信息
     * @see #of(String, boolean)
     */
    public static SqlInfo of(String sql) {
        return of(sql, true);
    }

    /**
     * 使用工厂快速构建 SQLInfo
     *
     * @param sql  sql 语句
     * @param sort 是否排序
     * @return 返回新的 SQLInfo
     */
    public static SqlInfo of(String sql, boolean sort) {
        SqlInfo info = new SqlInfo();
        info.setSql(sql);
        info.setOrderBy(sort);
        return info;
    }

    public static SqlInfo newInstance() {
        return new SqlInfo().setOrderBy(true);
    }
}
