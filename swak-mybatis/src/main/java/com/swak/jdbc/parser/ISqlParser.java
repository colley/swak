package com.swak.jdbc.parser;


import com.swak.jdbc.metadata.SqlInfo;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 14:23
 */
public interface ISqlParser {

    SqlInfo parser(String sql);
}
