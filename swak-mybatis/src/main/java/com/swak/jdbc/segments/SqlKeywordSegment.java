package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;

public class SqlKeywordSegment extends AbstractSqlSegment{

    private  Object value;

    public SqlKeywordSegment(String property, SqlKeyword sqlKeyword,Object value) {
        super(property, sqlKeyword);
        this.value = value;
    }

    @Override
    public String toString() {
        return property + getSqlKeyword().getKeyword() + value.toString();
    }


    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        String parameterName = valuePairs.addParameter(property, value);
        String fragment = property + getSqlKeyword().getSqlSegment(valuePairs) +
                IbsStringHelper.repeatParamFormat(parameterName);
        return fragment;
    }
}
