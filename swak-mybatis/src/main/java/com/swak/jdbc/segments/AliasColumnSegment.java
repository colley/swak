package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.common.util.StringPool;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


public class AliasColumnSegment extends AbstractSqlSegment implements ColumnSegment{

    @Getter
    private String columnName;
    @Getter
    private String tableAlias;

    private boolean hasAlias;

    public AliasColumnSegment() {
        super(SqlKeyword.APPLY);
    }

    public AliasColumnSegment(String columnName) {
        this(columnName,false, StringPool.EMPTY);
    }

    public AliasColumnSegment(String columnName, boolean hasAlias,String tableAlias) {
        super(SqlKeyword.APPLY);
        this.columnName = columnName;
        this.tableAlias = tableAlias;
        this.hasAlias = hasAlias;
    }

    @Override
    public String getProperty() {
        if (columnName.contains(StringPool.DOT) ||
                StringUtils.isEmpty(tableAlias)) {
            return columnName;
        }

        return hasAlias?tableAlias + "." + columnName: columnName;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return getProperty();
    }

    public static AliasColumnSegment as(String columnName) {
        return as(columnName, StringPool.EMPTY,StringPool.EMPTY);
    }
    public static AliasColumnSegment as(String columnName, String aliasName) {
        return as(columnName, aliasName,StringPool.EMPTY);
    }

    public static AliasColumnSegment alias(String columnName,String tableAlias) {
        return alias(columnName,true,tableAlias);
    }

    public static AliasColumnSegment alias(String columnName,boolean hasAlias,String tableAlias) {
        return new AliasColumnSegment(columnName,hasAlias,tableAlias);
    }

    public static AliasColumnSegment alias(String columnName, String aliasName,String tableAlias) {
        return as(columnName, aliasName,tableAlias);
    }

    public static AliasColumnSegment as(String columnName, String aliasName,String tableAlias) {
        return as(columnName,true,aliasName,tableAlias);
    }

    public static AliasColumnSegment as(String columnName,boolean hasAlias, String aliasName,String tableAlias) {
        if (StringUtils.isNotEmpty(aliasName)) {
            String newColumnName = columnName + " AS " + aliasName;
            return new AliasColumnSegment(newColumnName,hasAlias,tableAlias);
        }
        return new AliasColumnSegment(columnName,hasAlias,tableAlias);
    }
}
