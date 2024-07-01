package com.swak.jdbc.dialects;


import static com.swak.common.util.StringPool.QUESTION_MARK;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 15:04
 */
public class OracleDialect implements Dialect{
    @Override
    public DialectModel paginationSql(String originalSql, long offset, long limit) {
        limit = (offset >= 1) ? (offset + limit) : limit;
        String sql = "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " +
            originalSql + " ) TMP WHERE ROWNUM <=" + QUESTION_MARK + ") WHERE ROW_ID > " + QUESTION_MARK;
        return new DialectModel(sql, limit, offset).setConsumerChain();
    }
}
