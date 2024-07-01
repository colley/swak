
package com.swak.jdbc.segments;


import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;

public class PageSegment extends AbstractSqlSegment {
    private final long startPos;
    private final long pageSize;

    public PageSegment(long startPos, long pageSize, SqlKeyword sqlKeyword) {
        super(sqlKeyword);
        this.startPos = startPos;
        this.pageSize = pageSize;
    }


    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        String parameterNamePos = paramNameValuePairs.addParameter("limitStartPos", startPos);
        String parameterNameSize = paramNameValuePairs.addParameter("limitPageSize", pageSize);
        StringBuilder buffer = new StringBuilder();
        if ("limit".equalsIgnoreCase(getSqlKeyword().name())) {
            buffer.append(getSqlKeyword().getSqlSegment(paramNameValuePairs));
            buffer.append(IbsStringHelper.repeatParamFormat(parameterNamePos));
            buffer.append(",");
            buffer.append(IbsStringHelper.repeatParamFormat(parameterNameSize));
        }
        return buffer.toString();
    }
}
