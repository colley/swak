package com.swak.jdbc.conditions.update;

import com.google.common.collect.Lists;
import com.swak.common.util.JacksonUtils;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.annotation.FieldStrategy;
import com.swak.jdbc.common.Constants;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.conditions.AbstractLambdaWrapper;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.metadata.TableFieldInfo;
import com.swak.jdbc.metadata.TableInfo;
import com.swak.jdbc.metadata.TableList;
import com.swak.jdbc.segments.ColumnSegment;
import com.swak.jdbc.segments.MergeSegments;
import com.swak.jdbc.toolkit.TableHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LambdaInsertWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
public class LambdaSaveWrapper<T> extends AbstractLambdaWrapper<T, LambdaSaveWrapper<T>>
        implements SwakSave<LambdaSaveWrapper<T>, SFunction<T, ?>, T> {

    private Set<String> sqlColumns;

    private Map<String, String> sqlValues;

    @Override
    public LambdaSaveWrapper<T> getChildren() {
        return this;
    }

    public LambdaSaveWrapper() {
        // 如果无参构造函数，请注意实体 NULL 情况 SET 必须有否则 SQL 异常
        this((T) null);
    }

    public LambdaSaveWrapper(T entity) {
        this.sqlColumns = new LinkedHashSet<>();
        this.sqlValues = new HashMap<>();
        super.initNeed();
        this.setEntity(entity);
    }


    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlColumns)) {
            return null;
        }
        List<String> columns = Lists.newArrayList();
        List<String> values = Lists.newArrayList();
        for (String column : sqlColumns) {
            columns.add(column);
            if (Objects.nonNull(sqlValues.get(column))) {
                values.add(sqlValues.get(column));
            }
        }
        //属于insert into table () values ()
        StringBuilder buffer = new StringBuilder().append("(");
        buffer.append(String.join(StringPool.COMMA, columns)).append(")");
        if (CollectionUtils.isNotEmpty(values)) {
            buffer.append(" VALUES(");
            List<String> lastSqlValues = values.stream().map(item -> IbsStringHelper.repeatParamFormat(item))
                    .collect(Collectors.toList());
            buffer.append(String.join(StringPool.COMMA, lastSqlValues)).append(")");
        }
        return buffer.toString();
    }


    @Override
    public LambdaSaveWrapper<T> addColumn(SFunction<T, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            for (SFunction<T, ?> column : columns) {
                this.sqlColumns.add(columnToString(column, true));
            }
        }
        return typedThis;
    }

    @Override
    public LambdaSaveWrapper<T> addValue(SFunction<T, ?> column, Object value) {
        String genParamName = paramNameValuePairs.addParameter(Constants.WRAPPER_PARAM, value);
        this.sqlValues.put(columnToString(column, true), genParamName);
        return typedThis;
    }

    private LambdaSaveWrapper<T> addValue(String column, Object value) {
        String genParamName = paramNameValuePairs.addParameter(Constants.WRAPPER_PARAM, value);
        this.sqlValues.put(column, genParamName);
        return typedThis;
    }

    @Override
    public LambdaSaveWrapper<T> addColumn(Map<SFunction<T, ?>, Object> columnValue) {
        if (MapUtils.isNotEmpty(columnValue)) {
            columnValue.forEach((k, v) -> {
                addColumn(k);
                addValue(k, v);
            });
        }
        return typedThis;
    }

    @Override
    public LambdaSaveWrapper<T> from(String tableName) {
        super.tableName.setValue(tableName);
        return typedThis;
    }

    @Override
    public LambdaSaveWrapper<T> setEntity(T entity) {
        if (Objects.nonNull(entity)) {
            TableInfo tableInfo = TableHelper.get(entity.getClass());
            if (Objects.nonNull(tableInfo)) {
                this.resetColumn();
                from(tableInfo.getTableName());
                Map<String, Object> objectMap = JacksonUtils.convertValue(entity);
                for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                    setByFieldStrategy(fieldInfo, objectMap);
                }
            }
        }
        return typedThis;
    }

    private void setByFieldStrategy(TableFieldInfo fieldInfo, Map<String, Object> objectMap) {
        FieldStrategy fieldStrategy = Optional.ofNullable(fieldInfo.getInsertStrategy()).orElse(FieldStrategy.DEFAULT);
        Object value = objectMap.get(fieldInfo.getProperty());
        switch (fieldStrategy) {
            case DEFAULT:
            case NOT_NULL: {
                if (Objects.nonNull(value)) {
                    sqlColumns.add(fieldInfo.getColumn());
                    addValue(fieldInfo.getColumn(), value);
                }
            }
            case IGNORED: {
                sqlColumns.add(fieldInfo.getColumn());
                addValue(fieldInfo.getColumn(), value);
            }
            case NEVER:
            default: {
            }
        }
    }

    @Override
    public LambdaSaveWrapper<T> addColumn(boolean condition, SFunction<T, ?> column, Object val) {
        if (condition) {
            String columnStr = columnToString(column, true);
            sqlColumns.add(columnStr);
            String genParamName = paramNameValuePairs.addParameter(columnStr, val);
            sqlValues.put(columnStr, genParamName);
        }
        return typedThis;
    }


    @Override
    public void clear() {
        super.clear();
        resetColumn();
    }

    private void resetColumn() {
        sqlColumns.clear();
        sqlValues.clear();
    }

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected LambdaSaveWrapper<T> instance() {
        return instance(index, null, null, SharedString.emptyString());
    }

    @Override
    protected LambdaSaveWrapper<T> instanceEmpty() {
        return new LambdaSaveWrapper<>();
    }

    @Override
    protected LambdaSaveWrapper<T> instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName) {
        return new LambdaSaveWrapper<>(getEntity(), getEntityClass(), new ArrayList<>(), paramNameValuePairs,
                new MergeSegments(), new ArrayList<>(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString(),
                this.tableList, index, keyWord, joinClass, tableName);
    }


    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        StringBuffer sql = new StringBuffer();
        sql.append(SqlKeyword.INSERT.getKeyword()).append(" INTO ").append(getTableName().getValue());
        sql.append(getSqlSet()).append(getFromSqlSegment(valuePairs));
        return sql.toString();
    }

    // //属于 insert into table() select * from...
    private String getFromSqlSegment(ParamNameValuePairs valuePairs) {
        String whereSql = super.getSqlSegment(valuePairs);
        String columnSql = toColumnSqlString(valuePairs);
        if (StringUtils.isEmpty(columnSql)) {
            return StringPool.EMPTY;
        }
        return SqlKeyword.SELECT.getKeyword() + columnSql +
                SqlKeyword.FROM.getKeyword() + from.getValue()
                + whereSql;
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate(entity)
     */
    public LambdaSaveWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.sqlValues = new HashMap<>();
        this.sqlColumns = new LinkedHashSet<>();
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate(...)
     */
    protected LambdaSaveWrapper(T entity, Class<T> entityClass, List<ColumnSegment> selectColumns,
                                ParamNameValuePairs paramNameValuePairs, MergeSegments mergeSegments,
                                List<LambdaSaveWrapper<T>> onWrappers,
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
        this.sqlValues = new HashMap<>();
        this.sqlColumns = new LinkedHashSet<>();
    }
}
