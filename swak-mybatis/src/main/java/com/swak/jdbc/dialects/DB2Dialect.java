package com.swak.jdbc.dialects;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 15:13
 */
public class DB2Dialect implements Dialect{
    @Override
    public DialectModel paginationSql(String originalSql, long offset, long limit) {
        if (limit <= 0) {
            // 无效分页，返回原 SQL（无参数）
            return new DialectModel(originalSql, limit).setConsumer(true);
        }
        // 移除原始 SQL 末尾的分号（避免语法错误）
        String cleanSql = originalSql.trim();
        if (cleanSql.endsWith(";")) {
            cleanSql = cleanSql.substring(0, cleanSql.length() - 1);
        }
        String sql = cleanSql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        //第一个 ? 是 offset，第二个 ? 是 limit
        return new DialectModel(sql, offset, limit).setConsumerChain();
    }
}
