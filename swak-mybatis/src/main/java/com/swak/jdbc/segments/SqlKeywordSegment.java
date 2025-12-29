package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;


/**
 * @author colley.ma
 * @since 2.3.3
 **/
public class SqlKeywordSegment extends AbstractSqlSegment{

    private final Object value;

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
        return property + getSqlKeyword().getSqlSegment(valuePairs) +
                IbsStringHelper.repeatParamFormat(parameterName);
    }
}
