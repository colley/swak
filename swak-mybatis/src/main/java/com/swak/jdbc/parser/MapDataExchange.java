
package com.swak.jdbc.parser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;



public class MapDataExchange {
    public Object[] getData(ParameterMapping[] mappings, Map<String, Object> additionalParameter) {
    	if(ArrayUtils.isEmpty(mappings) || additionalParameter==null) {
    		return null;
    	}
        Object[] data = new Object[mappings.length];
        for (int i = 0; i < mappings.length; i++) {
            data[i] = additionalParameter.get(mappings[i].getPropertyName());
        }
        return data;
    }
}
