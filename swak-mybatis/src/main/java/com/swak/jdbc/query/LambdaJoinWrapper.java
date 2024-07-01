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
 * LambdaJoinWrapper
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/11 11:20
 **/
public class LambdaJoinWrapper<T> extends AbstractLambdaJoinWrapper<T, LambdaJoinWrapper<T>>
        implements SwakQuery<LambdaJoinWrapper<T>> {


    /**
     * 推荐使用 带 class 的构造方法
     */
    public LambdaJoinWrapper() {
        super();
    }

    /**
     * 推荐使用此构造方法
     */
    public LambdaJoinWrapper(Class<T> clazz) {
        super(clazz);
    }

    /**
     * 构造方法
     *
     * @param entity 主表实体
     */
    public LambdaJoinWrapper(T entity) {
        super(entity);
    }

    @Override
    public  List<ColumnSegment> getSelectFrom() {
        return selectColumns;
    }

    @Override
    public LambdaJoinWrapper<T> getChildren() {
        return this;
    }

    @Override
    public LambdaJoinWrapper<T> from(String tableName) {
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
    public final <E> LambdaJoinWrapper<T> select(SFunction<E, ?>... columns) {
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
   protected LambdaJoinWrapper(T entity, Class<T> entityClass, List<ColumnSegment> selectColumns,
                               ParamNameValuePairs paramNameValuePairs, MergeSegments mergeSegments,
                               List<LambdaJoinWrapper<T>> onWrappers,
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
    protected LambdaJoinWrapper<T> instance() {
        return instance(index, null, null,SharedString.emptyString());
    }

    @Override
    protected LambdaJoinWrapper<T> instanceEmpty() {
        return new LambdaJoinWrapper<>();
    }

    @Override
    protected LambdaJoinWrapper<T> instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName) {
        return new LambdaJoinWrapper<>(getEntity(), getEntityClass(),new ArrayList<>(),paramNameValuePairs,
                new MergeSegments(),new ArrayList<>(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString(),
                this.tableList, index, keyWord,joinClass,tableName);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        String columnSql = toColumnSqlString(valuePairs);
        String whereSql = super.getSqlSegment(valuePairs);
        if(StringUtils.isEmpty(columnSql)){
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
