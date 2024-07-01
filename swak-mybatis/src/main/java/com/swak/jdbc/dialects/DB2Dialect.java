package com.swak.jdbc.dialects;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 15:13
 */
public class DB2Dialect implements Dialect{
    @Override
    public DialectModel paginationSql(String originalSql, long offset, long limit) {
        return null;
    }
}
