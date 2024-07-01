package com.swak.jdbc.segments;

import com.google.common.collect.Lists;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author colley
 */
public  class MergeFromSegments implements SelectSegment {
    private final List<ColumnSegment> columnSegments = new ArrayList<>();
    @Getter
    protected List<SqlSegment> fromSegments = new ArrayList<>();
    protected List<SqlSegment> joinSegments = new ArrayList<>();

    @Override
    public SelectSegment select(List<ColumnSegment> columns) {
        if(CollectionUtils.isNotEmpty(columns)){
            columnSegments.addAll(columns);
        }
        return this;
    }

    public SelectSegment select(String column) {
        return select(column, StringPool.EMPTY,StringPool.EMPTY);
    }

    public SelectSegment select(String column, String tableAlias) {
        if (StringUtils.isNotEmpty(column)) {
            select(column,StringPool.EMPTY,tableAlias);
        }
        return this;
    }

    public SelectSegment select(String column, String aliasName, String tableAlias) {
        if (StringUtils.isNotEmpty(column)) {
            String[] splitColumns = StringUtils.split(column, StringPool.COMMA);
            List<ColumnSegment> lastColumns = Arrays.stream(splitColumns).map(it -> {
                if (it.contains(StringPool.DOT)) {
                    return AliasColumnSegment.as(it,aliasName);
                }
                return AliasColumnSegment.as(it,aliasName,tableAlias);
            }).collect(Collectors.toList());
            columnSegments.addAll(lastColumns);
        }
        return this;
    }

    public SelectSegment select(String tableAlias, String... columns) {
        return select(tableAlias,Arrays.asList(columns));
    }

    public SelectSegment select(String... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            select(StringPool.EMPTY,columns);
        }
        return this;
    }

    public SelectSegment select(ColumnSegment... sqlSegment) {
        if (ArrayUtils.isNotEmpty(sqlSegment)) {
            columnSegments.addAll(Lists.newArrayList(sqlSegment));
        }
        return this;
    }

    public SelectSegment select(String tableAlias, List<String> columns) {
        if (CollectionUtils.isEmpty(columns)) {
            return this;
        }
        for (String column : columns) {
            select(column,StringPool.EMPTY,tableAlias);
        }
        return this;
    }

    @Override
    public SelectSegment from(FromSegment fromSegment) {
        if (fromSegment != null) {
            fromSegments.add(fromSegment);
        }
        return this;
    }

    @Override
    public SelectSegment join(SqlSegment joinSegment) {
        if(joinSegment!=null){
            joinSegments.add(joinSegment);
        }
        return this;
    }

    protected String toColumnSqlString(ParamNameValuePairs paramNameValuePairs) {
        if(!hasSelectColumn()){
            return StringPool.EMPTY;
        }
        List<String> stringSelectColumn = columnSegments.stream().map(s -> s.getSqlSegment(paramNameValuePairs))
                .collect(Collectors.toList());
        return IbsStringHelper.join(",", stringSelectColumn.iterator());
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        StringBuilder builder = new StringBuilder();
        String selColumns = toColumnSqlString(paramNameValuePairs);
        builder.append(selColumns);
        if(StringUtils.isNotEmpty(selColumns)){
            builder.append(SqlKeyword.FROM.getKeyword()).append(fromSqlString(paramNameValuePairs));
        }
        String outerJoinsAfterFrom = fromJoinsSqlString(paramNameValuePairs);
        if (StringUtils.isNotEmpty(outerJoinsAfterFrom)) {
            builder.append(outerJoinsAfterFrom);
        }
        return builder.toString();
    }

    public boolean hasSelectColumn(){
        return CollectionUtils.isNotEmpty(columnSegments);
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return SqlKeyword.APPLY;
    }

    protected String fromSqlString(ParamNameValuePairs paramNameValuePairs) {
        if (CollectionUtils.isEmpty(fromSegments)) {
            return StringPool.EMPTY;
        }
        List<String> fromSqlClause = Lists.newArrayList();
        for (SqlSegment fromSegment : fromSegments) {
            fromSqlClause.add(fromSegment.getSqlSegment(paramNameValuePairs));
        }
        return IbsStringHelper.join(",", fromSqlClause.iterator());
    }

    protected String fromJoinsSqlString(ParamNameValuePairs paramNameValuePairs) {
        if (CollectionUtils.isEmpty(joinSegments)) {
            return StringPool.EMPTY;
        }
        List<String> fromClause = joinSegments.stream().map(clause ->
                clause.getSqlSegment(paramNameValuePairs)).collect(Collectors.toList());
        return IbsStringHelper.join(" ", fromClause.iterator());
    }
}
