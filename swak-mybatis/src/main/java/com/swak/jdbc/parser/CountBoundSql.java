package com.swak.jdbc.parser;

import com.swak.common.dto.SwakPage;
import com.swak.jdbc.metadata.SqlInfo;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 16:09
 */
public class CountBoundSql  extends StaticBoundSql{

    public CountBoundSql(SwakPage<?> page,SwakBoundSql boundSql){
        SqlInfo sqlInfo = SqlParserUtils.getOptimizeCountSql(page.optimizeCountSql(),boundSql.getSql());
        setSql(sqlInfo.getSql());
        setParameterMappings(boundSql.getParameterMapping());
        setAdditionalParameter(boundSql.getAdditionalParameters());
    }
}
