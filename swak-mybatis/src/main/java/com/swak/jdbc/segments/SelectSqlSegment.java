package com.swak.jdbc.segments;

import com.google.common.collect.Lists;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairTranslator;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.conditions.WhereStrWrapper;
import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *SelectSqlSegment
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:21
 **/
public class SelectSqlSegment extends WhereStrWrapper<SelectSqlSegment> implements SelectSegment {
    private final MergeFromSegments selectSegment = new MergeFromSegments();

    @Override
    protected SelectSqlSegment getChildren() {
        return this;
    }

    public SelectSqlSegment() {
        super();
    }

    @Override
    public SelectSqlSegment select(List<ColumnSegment> columns) {
         selectSegment.select(columns);
         return this;
    }

    public SelectSqlSegment select(String column) {
        return select(column, StringPool.EMPTY,getAlias().getValue());
    }

    public SelectSqlSegment select(String column,String tableAlias) {
        if (StringUtils.isNotEmpty(column)) {
            selectSegment.select(Lists.newArrayList(AliasColumnSegment.alias(column,tableAlias)));
        }
        return this;
    }

    public SelectSqlSegment select(String column, String aliasName,String tableAlias) {
        if (StringUtils.isNotEmpty(column)) {
            selectSegment.select(Lists.newArrayList(AliasColumnSegment.alias(column,aliasName,tableAlias)));
        }
        return this;
    }


    public SelectSqlSegment select(String... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            selectSegment.select(Arrays.stream(columns).map(column->AliasColumnSegment.alias(column,getAlias().getValue()))
                    .collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public SelectSqlSegment from(FromSegment fromSegment) {
        if(Objects.nonNull(fromSegment)){
            fromSegment.setAlias(getAlias().getValue());
            selectSegment.from(fromSegment);
        }
        return this;
    }

    @Override
    public SelectSegment join(SqlSegment joinSegment) {
        return selectSegment.join(joinSegment);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        String columnSql = selectSegment.getSqlSegment(valuePairs);
        String whereSql = super.getSqlSegment(valuePairs);
        if(StringUtils.isEmpty(columnSql)) {
            return whereSql;
        }
        StringBuilder builder = new StringBuilder(SqlKeyword.SELECT.getKeyword());
        builder.append(columnSql);
        if(StringUtils.isNotEmpty(whereSql)){
            builder.append(SqlKeyword.WHERE.getKeyword()).append(whereSql);
        }
        return builder.toString();
    }

    @Override
    protected SelectSqlSegment instance() {
        return new SelectSqlSegment(new MergeSegments(),alias,
                SharedString.emptyString(), SharedString.emptyString(),
                SharedString.emptyString());
    }


    private SelectSqlSegment(MergeSegments mergeSegments, SharedString tableAlias,
                             SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        this.expression = mergeSegments;
        this.alias = tableAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    public static void main(String[] args) {
        SelectSqlSegment selectSqlSegment = new SelectSqlSegment();
        //selectSqlSegment.setTableAlias("u");
        selectSqlSegment.select("*").from(TableFromSegment.from("user"))
                .eq("account", "admin")
                .eq("age", 3)
                .or(sql -> sql.eq("age", 4).eq("name", "colley"));
        System.out.println(selectSqlSegment.getSqlSegment(new ParamNameValuePairTranslator()));
    }

}
