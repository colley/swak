package com.swak.jdbc.segments;

import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SelectCache;
import org.apache.commons.lang3.StringUtils;

/**
 * ClassColumnSegment
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:04
 **/
public class ClassColumnSegment extends AbstractSqlSegment implements ColumnSegment {

    private  Integer index;

    private  SelectCache cache;

    private String aliasName;

    private  String tableAlias;

    private boolean hasTableAlias;

    public ClassColumnSegment() {
        super(SqlKeyword.APPLY);
    }

    public ClassColumnSegment(SelectCache cache, Integer index,boolean hasTableAlias, String tableAlias) {
       this(cache,index,null,hasTableAlias,tableAlias);
    }

    public  ClassColumnSegment(SelectCache cache, Integer index,String aliasName,boolean hasTableAlias, String tableAlias) {
        this();
        this.cache = cache;
        this.index = index;
        this.tableAlias = tableAlias;
        this.hasTableAlias = hasTableAlias;
        this.aliasName = aliasName;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        String prefix;
        if (hasTableAlias) {
            prefix = tableAlias;
        } else {
            prefix = valuePairs.getTableList().getPrefix(index, cache.getClazz(), false);
        }
        String columnName = cache.getColumn();
        if(StringUtils.isNotEmpty(aliasName)){
            columnName = columnName + " AS " + aliasName;
        }
        if(StringUtils.isNotEmpty(prefix)){
            return  prefix + StringPool.DOT + columnName;
        }
        return  columnName;
    }

    @Override
    public String getTableAlias() {
        return tableAlias;
    }
}
