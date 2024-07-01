package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

public class StringSqlSegment implements SqlSegment {

    private final static String WRAPPER_PARAM = "SWAKVAL";

    @Getter
    private String sqlStr;

    private Object[] params;

    public StringSqlSegment(String sqlStr) {
        this(sqlStr,null);
    }

    public StringSqlSegment(String sqlStr,Object[] params) {
        this.sqlStr = sqlStr;
        this.params = params;
    }

    public static SqlSegment apply(String sqlStr) {
        return new StringSqlSegment(sqlStr);
    }

    public static SqlSegment apply(String sqlStr,Object[] params) {
        return new StringSqlSegment(sqlStr,params);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        if (ArrayUtils.isNotEmpty(params)) {
            for (int i = 0; i < params.length; ++i) {
                String genParamName = valuePairs.addParameter(WRAPPER_PARAM, params[i]);
                sqlStr = sqlStr.replace(String.format("{%s}", i),
                        IbsStringHelper.repeatParamFormat(genParamName));
            }
        }
        return  sqlStr;
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return SqlKeyword.APPLY;
    }
}