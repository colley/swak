package com.swak.jdbc.parser;


import java.util.List;
import java.util.Map;

public class SwakStaticSqlSource implements HsSqlSource{
    private final String sql;
    private final List<ParameterMapping> parameterMappings;

    public SwakStaticSqlSource(String sql) {
        this(sql, null);
    }

    public SwakStaticSqlSource(String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
    }

    @Override
    public SwakBoundSql getBoundSql(Map<String, Object> additionalParameter) {
        StaticBoundSql boundSql = new StaticBoundSql();
        boundSql.setSql(sql);
        boundSql.setParameterMappings(parameterMappings);
        boundSql.setAdditionalParameter(additionalParameter);
        return boundSql;
    }
}
