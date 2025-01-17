package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairTranslator;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.Constants;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

public class StringSqlSegment implements SqlSegment {



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
                String genParamName = valuePairs.addParameter(Constants.WRAPPER_PARAM, params[i]);
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

    public static void main(String[] args) {
        ParamNameValuePairs valuePairs = new ParamNameValuePairTranslator();
        StringSqlSegment sqlSegment = new StringSqlSegment("{0}={1}",new String[]{"name","colley"});
        System.out.println(sqlSegment.getSqlSegment(valuePairs));
    }
}