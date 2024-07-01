package com.swak.jdbc.segments;

import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import lombok.Setter;

import java.util.Objects;

public class CombineSqlSegment implements SqlSegment {

    private final SqlKeyword sqlKeyword;

    @Setter
    private SqlSegment sqlSegment;

    public CombineSqlSegment(SqlKeyword sqlKeyword, SqlSegment sqlSegment) {
        this.sqlKeyword = sqlKeyword;
        this.sqlSegment = sqlSegment;
    }

    public static CombineSqlSegment combine(SqlKeyword sqlKeyword, SqlSegment sqlSegment) {
        return new CombineSqlSegment(sqlKeyword, sqlSegment);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        if (Objects.nonNull(sqlKeyword) && Objects.nonNull(sqlSegment)) {
            return sqlKeyword.getKeyword() + " " + sqlSegment.getSqlSegment(valuePairs);
        }
        return StringPool.EMPTY;
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return sqlKeyword;
    }
}
