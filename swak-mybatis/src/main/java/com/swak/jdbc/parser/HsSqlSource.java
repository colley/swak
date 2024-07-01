package com.swak.jdbc.parser;

import java.util.Map;

public interface  HsSqlSource {

    SwakBoundSql getBoundSql(Map<String, Object> additionalParameter);
}
