
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.common.util.StringPool;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


public class TableFromSegment extends AbstractSqlSegment implements FromSegment {

    @Getter
    private final String tableName;
    private String aliasTableName;

    public TableFromSegment(String tableName) {
       this(tableName, StringPool.EMPTY);
    }

    public TableFromSegment(String tableName, String aliasTableName) {
        super(SqlKeyword.APPLY);
        this.tableName = tableName;
        this.aliasTableName = aliasTableName;
    }

    public static TableFromSegment from(String tableName) {
        return new TableFromSegment(tableName);
    }

    public static TableFromSegment from(String tableName, String aliasName) {
        return new TableFromSegment(tableName, aliasName);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        if(StringUtils.isEmpty(getAlias())){
            return tableName;
        }
        return tableName + " " + getAlias();
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
