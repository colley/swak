
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;

public class SubSelectSegment extends AbstractSqlSegment {
    private final SqlSegment selectSegment;

    public SubSelectSegment(String property, SqlSegment selectSegment, SqlKeyword sqlKeyword) {
        super(property,sqlKeyword);
        this.selectSegment = selectSegment;
    }

    @Override
	public String toString() {
        return property + getSqlKeyword() + selectSegment.toString();
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        return getProperty() + getSqlSegment(paramNameValuePairs) + " (" +
                selectSegment.getSqlSegment(paramNameValuePairs) +
                ")";
    }
}
