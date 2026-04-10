package com.swak.jdbc.conditions.query;

import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.conditions.AbstractLambdaWrapper;
import com.swak.jdbc.conditions.SwakQuery;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.metadata.SelectCache;
import com.swak.jdbc.metadata.TableInfo;
import com.swak.jdbc.metadata.TableList;
import com.swak.jdbc.segments.ClassColumnSegment;
import com.swak.jdbc.segments.ColumnSegment;
import com.swak.jdbc.segments.MergeSegments;
import com.swak.jdbc.toolkit.LambdaUtils;
import com.swak.jdbc.toolkit.TableHelper;
import com.swak.jdbc.toolkit.support.ColumnCache;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * LambdaJoinWrapper
 *
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/11 11:20
 **/
public class LambdaQueryWrapper<T> extends AbstractLambdaWrapper<T, LambdaQueryWrapper<T>>
        implements SwakQuery<LambdaQueryWrapper<T>> {


    /**
     * 推荐使用 带 class 的构造方法
     */
    public LambdaQueryWrapper() {
        super();
    }

    /**
     * 推荐使用此构造方法
     */
    public LambdaQueryWrapper(Class<T> clazz) {
        super(clazz);
    }

    /**
     * 构造方法
     *
     * @param entity 主表实体
     */
    public LambdaQueryWrapper(T entity) {
        super(entity);
    }

    @Override
    public List<ColumnSegment> getSelectFrom() {
        return selectColumns;
    }

    @Override
    public LambdaQueryWrapper<T> getChildren() {
        return this;
    }

    @Override
    public LambdaQueryWrapper<T> from(String tableName) {
        this.from(tableName, null);
        return this;
    }

    @Override
    public LambdaQueryWrapper<T> from(String tableName, String tableAlias) {
        this.tableName.setValue(tableName);
        if (StringUtils.isNotEmpty(tableAlias)) {
            this.alias.setValue(tableAlias);
        }
        return this;
    }

    @Override
    public LambdaQueryWrapper<T> setEntity(T entity) {
        super.setEntity(entity);
        TableInfo tableInfo = TableHelper.get(getEntityClass());
        if (Objects.nonNull(tableInfo)) {
            this.from(tableInfo.getTableName());
        }
        return this;
    }

    @Override
    public LambdaQueryWrapper<T> setEntityClass(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        if (Objects.nonNull(entityClass)) {
            TableInfo tableInfo = TableHelper.get(entityClass);
            if (Objects.nonNull(tableInfo)) {
                this.from(tableInfo.getTableName());
            }
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
    public final <E> LambdaQueryWrapper<T> select(SFunction<E, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            Class<?> aClass = LambdaUtils.getEntityClass(columns[0]);
            Map<String, SelectCache> cacheMap = ColumnCache.getMapField(aClass);
            for (SFunction<E, ?> s : columns) {
                SelectCache cache = cacheMap.get(LambdaUtils.getName(s));
                getSelectFrom().add(new ClassColumnSegment(cache, index.getValue(), hasAlias.getValue(), alias.getValue()));
            }
        }
        return typedThis;
    }

    /**
     * 不建议直接 new 该实例，使用 JoinWrappers.lambda(UserDO.class)
     */
    protected LambdaQueryWrapper(T entity, Class<T> entityClass, List<ColumnSegment> selectColumns,
                                 ParamNameValuePairs paramNameValuePairs, MergeSegments mergeSegments,
                                 List<LambdaQueryWrapper<T>> onWrappers,
                                 SharedString lastSql,
                                 SharedString sqlComment, SharedString sqlFirst,
                                 TableList tableList, SharedInteger index, SqlKeyword sqlKeyword,
                                 Class<?> joinClass, SharedString tableName) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.selectColumns = selectColumns;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.onWrappers = onWrappers;
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
    protected LambdaQueryWrapper<T> instance() {
        return instance(index, null, null, SharedString.emptyString());
    }

    @Override
    protected LambdaQueryWrapper<T> instanceEmpty() {
        return new LambdaQueryWrapper<>();
    }

    @Override
    protected LambdaQueryWrapper<T> instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName) {
        return new LambdaQueryWrapper<>(getEntity(), getEntityClass(), new ArrayList<>(), paramNameValuePairs,
                new MergeSegments(), new ArrayList<>(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString(),
                this.tableList, index, keyWord, joinClass, tableName);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        String columnSql = toColumnSqlString(valuePairs);
        String whereSql = super.getSqlSegment(valuePairs);
        if (StringUtils.isEmpty(columnSql)) {
            return whereSql;
        }
        String builder = SqlKeyword.SELECT.getKeyword() +
                columnSql + SqlKeyword.FROM +
                getTableName().getValue() +
                StringPool.SPACE +
                getAlias().getValue() +
                whereSql;
        return builder;
    }

}
