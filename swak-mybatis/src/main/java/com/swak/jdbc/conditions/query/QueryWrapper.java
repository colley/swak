package com.swak.jdbc.conditions.query;

import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.conditions.AbstractLambdaWrapper;
import com.swak.jdbc.conditions.AbstractWrapper;
import com.swak.jdbc.conditions.SwakQuery;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.metadata.SelectCache;
import com.swak.jdbc.metadata.TableList;
import com.swak.jdbc.segments.ClassColumnSegment;
import com.swak.jdbc.segments.ColumnSegment;
import com.swak.jdbc.segments.MergeSegments;
import com.swak.jdbc.toolkit.LambdaUtils;
import com.swak.jdbc.toolkit.support.ColumnCache;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * QueryJdbcWrapper
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:01
 **/
public class QueryWrapper<T> extends AbstractLambdaWrapper<T, QueryWrapper<T>>
        implements SwakQuery<QueryWrapper<T>> {
    /**
     * 推荐使用 带 class 的构造方法
     */
    public QueryWrapper() {
        super();
    }

    /**
     * 推荐使用此构造方法
     */
    public QueryWrapper(Class<T> clazz) {
        super(clazz);
    }

    /**
     * 构造方法
     *
     * @param entity 主表实体
     */
    public QueryWrapper(T entity) {
        super(entity);
    }

    @Override
    public List<ColumnSegment> getSelectFrom() {
        return super.selectColumns;
    }

    @Override
    public QueryWrapper<T> getChildren() {
        return this;
    }

    @Override
    public QueryWrapper<T> from(String tableName) {
        this.from(tableName,null);
        return this;
    }

    @Override
    public QueryWrapper<T> from(String tableName, String tableAlias) {
        this.tableName.setValue(tableName);
        if(StringUtils.isNotEmpty(tableAlias)){
            this.alias.setValue(tableAlias);
        }
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
    public final <E> QueryWrapper<T> select(SFunction<E, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            Class<?> aClass = LambdaUtils.getEntityClass(columns[0]);
            Map<String, SelectCache> cacheMap = ColumnCache.getMapField(aClass);
            for (SFunction<E, ?> s : columns) {
                SelectCache cache = cacheMap.get(LambdaUtils.getName(s));
                this.selectColumns.add(new ClassColumnSegment(cache, index.getValue(), hasAlias.isTrue(), alias.getValue()));
            }
        }
        return this;
    }

    /**
     * 不建议直接 new 该实例，使用 JoinWrappers.lambda(UserDO.class)
     */
    QueryWrapper(T entity, Class<T> entityClass, List<ColumnSegment> selectColumns,
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
    protected QueryWrapper<T> instance() {
        return instance(index, null, null, SharedString.emptyString());
    }

    @Override
    protected QueryWrapper<T> instanceEmpty() {
        return new QueryWrapper<>();
    }

    @Override
    protected QueryWrapper<T> instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName) {
        return new QueryWrapper<>(getEntity(), getEntityClass(), new ArrayList<>(), paramNameValuePairs,
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

    public LambdaQueryWrapper<T> lambda() {
        return new LambdaQueryWrapper(this.getEntity(), this.getEntityClass(),
                this.selectColumns, this.paramNameValuePairs, this.expression,this.onWrappers,
                this.lastSql, this.sqlComment, this.sqlFirst, this.tableList, this.index,
                this.sqlKeyword, this.joinClass, this.tableName);
    }
}
