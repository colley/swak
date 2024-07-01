package com.swak.jdbc.dialects;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 14:55
 */
public interface Dialect {
    DialectModel paginationSql(String originalSql, long offset, long limit);
}
