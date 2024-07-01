package com.swak.jdbc.segments;

import com.google.common.collect.Lists;
import com.swak.jdbc.toolkit.JdbcRestrictions;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlJoinSegment extends AbstractSqlSegment implements JoinSegment{

    private final String tableName;

    private SelectSegment selectSegment;
    private final SqlSegment onClause;
    private List<SqlSegment> whereScope = new ArrayList<>();

    public SqlJoinSegment(SqlKeyword sqlKeyword, String aliasTableName, SelectSegment selectSegment, String[] onClauses) {
        super(sqlKeyword);
        this.tableName = aliasTableName;
        this.selectSegment = selectSegment;
        this.onClause = JdbcRestrictions.eqProperty(onClauses[0], onClauses[1]);
    }

    public static JoinSegment leftJoin(String aliasTableName, SelectSegment selectSegment, String[] onClauses) {
        return new SqlJoinSegment(SqlKeyword.LEFT_JOIN, aliasTableName,selectSegment, onClauses);
    }

    public static JoinSegment rightJoin(String aliasTableName, SelectSegment selectSegment,String[] onClauses) {
        return new SqlJoinSegment(SqlKeyword.RIGHT_JOIN, aliasTableName,selectSegment, onClauses);
    }

    public static JoinSegment join(String aliasTableName, SelectSegment selectSegment,String[] onClauses) {
        return new SqlJoinSegment(SqlKeyword.JOIN, aliasTableName,selectSegment,onClauses);
    }
    public static JoinSegment innerJoin(String aliasTableName, SelectSegment selectSegment,String[] onClauses) {
        return new SqlJoinSegment(SqlKeyword.INNER_JOIN, aliasTableName,selectSegment,onClauses);
    }




    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        StringBuilder sql = new StringBuilder(" ");
        sql.append(getSqlKeyword().getKeyword());
        sql.append("(");
        sql.append(selectSegment.getSqlSegment(valuePairs));
        sql.append(") ");
        sql.append(tableName);
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
