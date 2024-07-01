
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import lombok.Getter;


public class SimpleSegment extends AbstractSqlSegment {
    @Getter
    private final Object value;

    @Getter
    private final boolean ignoreCase;

    public SimpleSegment(String property, SqlKeyword sqlKeyword) {
        this(property, null, sqlKeyword, false);
    }

    public SimpleSegment(String property, Object value, SqlKeyword sqlKeyword) {
        this(property, value, sqlKeyword, false);
    }

    public SimpleSegment(String property, Object value, SqlKeyword sqlKeyword, boolean ignoreCase) {
        super(property, sqlKeyword);
        this.value = value;
        this.ignoreCase = ignoreCase;
    }


    @Override
    public String toString() {
        return property + getSqlKeyword().getKeyword() + value.toString();
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        String parameterName = paramNameValuePairs.addParameter(property, value);
        String fragment = property +
                getSqlKeyword().getSqlSegment(paramNameValuePairs) +
                IbsStringHelper.repeatParamFormat(parameterName);
        return fragment;
    }
}
