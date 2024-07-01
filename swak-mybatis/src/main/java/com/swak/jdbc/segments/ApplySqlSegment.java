package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ApplySqlSegment extends ArrayList<SqlSegment> implements SqlSegment {


    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        List<String> applySqlList = this.stream().map(sqlSegment -> sqlSegment.getSqlSegment(valuePairs))
                .collect(Collectors.toList());
        return IbsStringHelper.join("", applySqlList.iterator());
    }


    public static ApplySqlSegment apply( String sqlStr, Object... params) {
        ApplySqlSegment applySqlSegment = new ApplySqlSegment();
        applySqlSegment.add(new StringSqlSegment(sqlStr,params));
        return applySqlSegment;
    }

    public ApplySqlSegment  apply(String... sql) {
        if (ArrayUtils.isNotEmpty(sql)) {
            for (String sqlStr : sql) {
                add(new StringSqlSegment(sqlStr));
            }
        }
        return this;
    }

    public ApplySqlSegment apply(SqlSegment... sqlSegments) {
        if (ArrayUtils.isNotEmpty(sqlSegments)) {
            for (SqlSegment sqlSegment : sqlSegments) {
                add(sqlSegment);
            }
        }
        return this;
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return SqlKeyword.APPLY;
    }

    public static void main(String[] args) {
        ApplySqlSegment applySqlSegment = new ApplySqlSegment();
        applySqlSegment.apply("name=22","age=333");
        System.out.println(applySqlSegment.getSqlSegment(null));
    }

}
