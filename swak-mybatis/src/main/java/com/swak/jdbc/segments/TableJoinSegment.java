
package com.swak.jdbc.segments;

import com.google.common.collect.Lists;
import com.swak.jdbc.toolkit.JdbcRestrictions;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.common.util.StringPool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;


public class TableJoinSegment extends AbstractSqlSegment implements JoinSegment {
    private final String tableName;
    private final String aliasTableName;
    private SqlSegment onClause;
    private List<SqlSegment> whereScope = new ArrayList<>();

    public TableJoinSegment(SqlKeyword sqlKeyword, String tableName, String[] onClauses) {
        this(sqlKeyword, tableName, StringPool.EMPTY, onClauses);
    }

    public TableJoinSegment(SqlKeyword sqlKeyword, String tableName, String aliasTableName, String[] onClauses) {
        super(sqlKeyword);
        if(onClauses.length==1){
            this.onClause = StringSqlSegment.apply(onClauses[0]);
        }
        if(onClauses.length >=2){
            this.onClause = JdbcRestrictions.eqProperty(onClauses[0], onClauses[1]);
        }
        this.tableName = tableName;
        this.aliasTableName = aliasTableName;
    }


    @Override
	public String toString() {
        return this.getSqlKeyword() + " " + tableName + " ON " + whereScope.iterator();
    }

    public static TableJoinSegment leftJoin(String tableName, String[] onClauses) {
        return new TableJoinSegment(SqlKeyword.LEFT_JOIN, tableName, onClauses);
    }

    public static TableJoinSegment rightJoin(String tableName, String[] onClauses) {
        return new TableJoinSegment(SqlKeyword.RIGHT_JOIN, tableName, onClauses);
    }

    public static TableJoinSegment join(String tableName, String[] onClauses) {
        return new TableJoinSegment(SqlKeyword.JOIN, tableName, onClauses);
    }

    public static TableJoinSegment leftJoin(String tableName, String aliasTableName, String... onClauses) {
        return new TableJoinSegment(SqlKeyword.LEFT_JOIN, tableName, aliasTableName, onClauses);
    }

    public static TableJoinSegment rightJoin(String tableName, String aliasTableName, String[] onClauses) {
        return new TableJoinSegment(SqlKeyword.RIGHT_JOIN, tableName, aliasTableName, onClauses);
    }

    public static TableJoinSegment join(String tableName, String aliasTableName, String[] onClauses) {
        return new TableJoinSegment(SqlKeyword.JOIN, tableName, aliasTableName, onClauses);
    }

    public static TableJoinSegment join(String tableName, String aliasTableName, String[] onClauses, SqlSegment[] whereScope) {
        TableJoinSegment tableJoinSegment = new TableJoinSegment(SqlKeyword.JOIN, tableName, aliasTableName, onClauses);
        if (ArrayUtils.isNotEmpty(whereScope)) {
            tableJoinSegment.where(whereScope);
        }
        return tableJoinSegment;
    }

    public static TableJoinSegment leftJoin(String tableName, String aliasTableName, String[] onClauses, SqlSegment[] whereScope) {
        TableJoinSegment tableJoinSegment = new TableJoinSegment(SqlKeyword.LEFT_JOIN, tableName, aliasTableName, onClauses);
        if (ArrayUtils.isNotEmpty(whereScope)) {
            tableJoinSegment.where(whereScope);
        }
        return tableJoinSegment;
    }

    public static TableJoinSegment rightJoin(String tableName, String aliasTableName, String[] onClauses, SqlSegment[] whereScope) {
        TableJoinSegment tableJoinSegment = new TableJoinSegment(SqlKeyword.RIGHT_JOIN, tableName, aliasTableName, onClauses);
        if (ArrayUtils.isNotEmpty(whereScope)) {
            tableJoinSegment.where(whereScope);
        }
        return tableJoinSegment;
    }

    public static TableJoinSegment innerJoin(String tableName, String aliasTableName, String[] onClauses, SqlSegment[] whereScope) {
        TableJoinSegment tableJoinSegment = new TableJoinSegment(SqlKeyword.INNER_JOIN, tableName, aliasTableName, onClauses);
        if (ArrayUtils.isNotEmpty(whereScope)) {
            tableJoinSegment.where(whereScope);
        }
        return tableJoinSegment;
    }
    
    public static TableJoinSegment join(String tableName, String aliasTableName, String[] onClauses, SqlSegment[] whereScope,
                                        SqlKeyword sqlKeyword) {
        TableJoinSegment tableJoinSegment = new TableJoinSegment(sqlKeyword, tableName, aliasTableName, onClauses);
        if (ArrayUtils.isNotEmpty(whereScope)) {
            tableJoinSegment.where(whereScope);
        }
        return tableJoinSegment;
    }

    public static TableJoinSegment innerJoin(String tableName, String aliasTableName, String[] onClauses) {
        return new TableJoinSegment(SqlKeyword.INNER_JOIN, tableName, aliasTableName, onClauses);
    }

    public static TableJoinSegment innerJoin(String tableName, String[] onClauses) {
        return new TableJoinSegment(SqlKeyword.INNER_JOIN, tableName, onClauses);
    }


    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        StringBuilder sql = new StringBuilder(" ");
        sql.append(getSqlKeyword().getSqlSegment(valuePairs))
                .append(tableName).append(" ").append(aliasTableName);
        sql.append(" ON ").append(onClause.getSqlSegment(valuePairs));
        if (CollectionUtils.isNotEmpty(whereScope)) {
            for (SqlSegment sqlSegment : whereScope) {
                sql.append(SqlKeyword.AND.getKeyword()).append(sqlSegment.getSqlSegment(valuePairs));
            }
        }
        return sql.toString();
    }

    @Override
    public JoinSegment where(SqlSegment... sqlSegment) {
        if(ArrayUtils.isNotEmpty(sqlSegment)){
            whereScope.addAll(Lists.newArrayList(sqlSegment));
        }
        return this;
    }
}
