package com.swak.jdbc.segments;

import com.swak.jdbc.toolkit.JdbcRestrictions;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OnSqlSegmentList extends ArrayList<SqlSegment> implements SqlSegment {
    private List<OnSqlSegment> onSqlSegmentList = new ArrayList<>();

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        List<String> stringList = onSqlSegmentList.stream()
                .map(onSqlSegment -> onSqlSegment.getSqlSegment(valuePairs)).collect(Collectors.toList());
        return StringUtils.join(stringList, getSqlKeyword().getSqlSegment(valuePairs));
    }

    public OnSqlSegmentList on(String firstAlias,String firstProperty,String secondAlias,String secondProperty){
        this.add(OnSqlSegment.on(firstAlias,firstProperty,secondAlias,secondProperty));
        return this;
    }

    public OnSqlSegmentList on(String firstProperty,String secondProperty){
        this.add(JdbcRestrictions.eqProperty(firstProperty,secondProperty));
        return this;
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return SqlKeyword.AND;
    }
}
