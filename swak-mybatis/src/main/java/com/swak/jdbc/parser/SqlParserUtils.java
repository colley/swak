package com.swak.jdbc.parser;

import com.swak.jdbc.metadata.SqlInfo;
import java.util.Optional;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 14:23
 */
public class SqlParserUtils {

    private static final ISqlParser COUNT_SQL_PARSER = new JsqlParserCountOptimize();

    /**
     * 获取 COUNT 原生 SQL 包装
     *
     * @param originalSql ignore
     * @return ignore
     */
    public static String getOriginalCountSql(String originalSql) {
        return String.format("SELECT COUNT(*) FROM (%s) TOTAL", originalSql);
    }

    public static SqlInfo getOptimizeCountSql(boolean optimizeCountSql, ISqlParser sqlParser, String originalSql) {
        if (optimizeCountSql) {
            return Optional.ofNullable(sqlParser).orElse(COUNT_SQL_PARSER).parser(originalSql);
        }
        return SqlInfo.newInstance().setSql(getOriginalCountSql(originalSql));
    }

    public static SqlInfo getOptimizeCountSql(boolean optimizeCountSql, String originalSql) {
       return getOptimizeCountSql(optimizeCountSql,null,originalSql);
    }
}
