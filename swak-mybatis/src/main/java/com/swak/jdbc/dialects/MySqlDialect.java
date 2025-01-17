package com.swak.jdbc.dialects;


import com.swak.common.util.StringPool;

import static com.swak.common.util.StringPool.QUESTION_MARK;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 14:58
 */
public class MySqlDialect implements Dialect{

    @Override
    public DialectModel paginationSql(String originalSql, long offset, long limit) {
        StringBuilder sql = new StringBuilder(originalSql).append(" LIMIT ").append(QUESTION_MARK);
        if (offset != 0L) {
            sql.append(StringPool.COMMA).append(QUESTION_MARK);
            return new DialectModel(sql.toString(), offset, limit).setConsumerChain();
        } else {
            return new DialectModel(sql.toString(), limit).setConsumer(true);
        }
    }
}
