package com.swak.jdbc.parser;

import java.util.List;
import java.util.Map;

public class SwakProviderSqlSource  implements HsSqlSource{

    private final String sql;
    private final List<ParameterMapping> parameterMappings;

    public SwakProviderSqlSource(String sql) {
        this(sql, null);
    }

    public SwakProviderSqlSource(String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
    }

    @Override
    public SwakBoundSql getBoundSql(Map<String, Object> additionalParameter) {
        ProviderBoundSql boundSql = new ProviderBoundSql();
        boundSql.setSql(sql);
        boundSql.setParameterMappings(parameterMappings);
        boundSql.setAdditionalParameter(additionalParameter);
        return boundSql;
    }
}
