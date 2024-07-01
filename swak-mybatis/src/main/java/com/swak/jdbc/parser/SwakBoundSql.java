
package com.swak.jdbc.parser;


import java.util.List;
import java.util.Map;

public interface SwakBoundSql extends java.io.Serializable {
     String getSql();
	 Object[] getParamObjectValues();

    Map<String, Object> getAdditionalParameters();

    List<ParameterMapping> getParameterMapping();
}
