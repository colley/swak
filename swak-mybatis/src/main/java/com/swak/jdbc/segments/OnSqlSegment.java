package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.common.util.StringPool;

public class OnSqlSegment extends AbstractSqlSegment {

    private  String firstAlias;

    private  String secondAlias;

    private String secondProperty;

    public OnSqlSegment(String firstAlias,String firstProperty,String secondAlias,String secondProperty,SqlKeyword sqlKeyword) {
        super(firstProperty, sqlKeyword);
        this.firstAlias = firstAlias;
        this.secondAlias = secondAlias;
        this.secondProperty = secondProperty;
    }

    public static OnSqlSegment on(String firstAlias,String firstProperty,String secondAlias,String secondProperty){
        return new OnSqlSegment(firstAlias,firstProperty,secondAlias,secondProperty,SqlKeyword.EQ);
    }

    public static OnSqlSegment on(String firstAlias,String firstProperty,String secondAlias,String secondProperty,SqlKeyword sqlKeyword){
        return new OnSqlSegment(firstAlias,firstProperty,secondAlias,secondProperty,sqlKeyword);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return firstAlias + StringPool.DOT + getProperty() +
                getSqlKeyword().getKeyword() + secondAlias + StringPool.DOT +
                secondProperty;
    }
}
