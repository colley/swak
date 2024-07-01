
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;


public class SqlFromSegment extends AbstractSqlSegment implements FromSegment {

    private  SelectSegment selectSegment;
    private String aliasTableName;

    public SqlFromSegment(SelectSegment selectSegment, String aliasTableName) {
        super(SqlKeyword.APPLY);
        this.selectSegment = selectSegment;
        this.aliasTableName = aliasTableName;
    }

    public static SqlFromSegment from(SelectSegment selectSegment,String aliasTableName) {
        return new SqlFromSegment(selectSegment,aliasTableName);
    }
    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return "(" + selectSegment.getSqlSegment(valuePairs) + ") " + getAlias();
    }

    @Override
    public void setAlias(String aliasTableName) {
            this.aliasTableName = aliasTableName;
    }

    @Override
    public String getAlias() {
        return aliasTableName;
    }
}
