
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;

/**
 * limit 10
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:15
 **/
public class LimitSegment extends AbstractSqlSegment {

    private final int limit;

    protected LimitSegment(int limit) {
        super("limit", SqlKeyword.LIMIT);
        this.limit = limit;
    }
    public static LimitSegment limit(int limit) {
    	return new LimitSegment(limit);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
    	String parameterNamePos = paramNameValuePairs.addParameter("limit", limit);
        String buffer = getSqlKeyword().getSqlSegment(paramNameValuePairs) +
                IbsStringHelper.repeatParamFormat(parameterNamePos);
        return buffer;
    }
}
