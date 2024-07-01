package com.swak.jdbc.dialects;

import static com.swak.common.util.StringPool.QUESTION_MARK;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 15:13
 */
public class SQLServerDialect implements Dialect{
    @Override
    public DialectModel paginationSql(String originalSql, long offset, long limit) {
        String sql = originalSql + " OFFSET " + QUESTION_MARK + " ROWS FETCH NEXT " + QUESTION_MARK + " ROWS ONLY";
        return new DialectModel(sql, offset, limit).setConsumerChain();
    }
}
