package com.swak.jdbc.query;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.conditions.SwakQuery;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SelectCache;
import com.swak.jdbc.metadata.TableList;
import com.swak.jdbc.segments.*;
import com.swak.jdbc.toolkit.LambdaUtils;
import com.swak.jdbc.toolkit.support.ColumnCache;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * QueryJoinWrapper
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:01
 **/
public class QueryJoinWrapper<T> extends AbstractJoinWrapper<T, QueryJoinWrapper<T>>
        implements SwakQuery<QueryJoinWrapper<T>> {
    /**
     * 推荐使用 带 class 的构造方法
     */
    public QueryJoinWrapper() {
        super();
    }

    /**
     * 推荐使用此构造方法
     */
    public QueryJoinWrapper(Class<T> clazz) {
        super(clazz);
    }

    /**
     * 构造方法
     *
     * @param entity 主表实体
     */
    public QueryJoinWrapper(T entity) {
        super(entity);
    }

    @Override
    public List<ColumnSegment> getSelectFrom() {
        return selectColumns;
    }

    @Override
    public QueryJoinWrapper<T> getChildren() {
        return this;
    }

    @Override
    public QueryJoinWrapper<T> from(String tableName) {
        this.tableName.setValue(tableName);
        return this;
    }

    /**
     * 设置查询字段
     *
     * @param columns 字段数组
     * @return children
     */
    @Override
    @SafeVarargs
    public final <E> QueryJoinWrapper<T> select(SFunction<E, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            Class<?> aClass = LambdaUtils.getEntityClass(columns[0]);
            Map<String, SelectCache> cacheMap = ColumnCache.getMapField(aClass);
            for (SFunction<E, ?> s : columns) {
                SelectCache cache = cacheMap.get(LambdaUtils.getName(s));
                getSelectFrom().add(new ClassColumnSegment(cache, index.getValue(), hasAlias.isTrue(), alias.getValue()));
            }
        }
        return this;
    }

    /**
     * 不建议直接 new 该实例，使用 JoinWrappers.lambda(UserDO.class)
     */
    QueryJoinWrapper(T entity, Class<T> entityClass, List<ColumnSegment> selectColumns,
                     ParamNameValuePairs paramNameValuePairs, MergeSegments mergeSegments,
                     SharedString lastSql,
                     SharedString sqlComment, SharedString sqlFirst,
                     TableList tableList, SharedInteger index, SqlKeyword sqlKeyword,
                     Class<?> joinClass, SharedString tableName) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.selectColumns = selectColumns;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
        this.tableList = tableList;
        this.index = index;
        this.sqlKeyword = sqlKeyword;
        this.joinClass = joinClass;
        this.tableName = tableName;
    }

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected QueryJoinWrapper<T> instance() {
        return instance(index, null, null, SharedString.emptyString());
    }

    @Override
    protected QueryJoinWrapper<T> instanceEmpty() {
        return new QueryJoinWrapper<>();
    }

    @Override
    protected QueryJoinWrapper<T> instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName) {
        return new QueryJoinWrapper<>(getEntity(), getEntityClass(), new ArrayList<>(), paramNameValuePairs,
                new MergeSegments(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString(),
                this.tableList, index, keyWord, joinClass, tableName);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        String columnSql = toColumnSqlString(valuePairs);
        String whereSql = super.getSqlSegment(valuePairs);
        if (StringUtils.isEmpty(columnSql)) {
            return whereSql;
        }
        return SqlKeyword.SELECT.getKeyword() +
                columnSql + SqlKeyword.FROM +
                getTableName().getValue() +
                StringPool.SPACE +
                getAlias().getValue() +
                whereSql;
    }

    public LambdaJoinWrapper<T> lambda() {
        return new LambdaJoinWrapper(this.getEntity(), this.getEntityClass(),
                this.selectColumns, this.paramNameValuePairs, this.expression,this.onWrappers,
                this.lastSql, this.sqlComment, this.sqlFirst, this.tableList, this.index,
                this.sqlKeyword, this.joinClass, this.tableName);
    }
}
