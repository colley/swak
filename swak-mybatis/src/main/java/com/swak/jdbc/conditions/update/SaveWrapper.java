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
 * InsertWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
public class SaveWrapper<T> extends AbstractLambdaWrapper<T, SaveWrapper<T>>
        implements SwakSave<SaveWrapper<T>, String, T> {


    private Set<String> sqlColumns;

    private Map<String, String> sqlValues;

    @Override
    public SaveWrapper<T> getChildren() {
        return this;
    }

    public SaveWrapper() {
        // 如果无参构造函数，请注意实体 NULL 情况 SET 必须有否则 SQL 异常
        this((T) null);
    }

    public SaveWrapper(T entity) {
        super.initNeed();
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
    public SaveWrapper<T> addColumn(String... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            sqlColumns.addAll(Lists.newArrayList(columns));
        }
        return typedThis;
    }

    @Override
    public SaveWrapper<T> addValue(String column, Object value) {
        String genParamName = paramNameValuePairs.addParameter(column, value);
        this.sqlValues.put(column, genParamName);
        return typedThis;
    }


    @Override
    public SaveWrapper<T> addColumn(Map<String, Object> columnValue) {
        if (MapUtils.isNotEmpty(columnValue)) {
            columnValue.forEach((k, v) -> {
                addColumn(k);
                addValue(k,v);
            });
        }
        return typedThis;
    }

    @Override
    public SaveWrapper<T> from(String tableName) {
        super.tableName.setValue(tableName);
        return typedThis;
    }

    @Override
    public SaveWrapper<T> setEntity(T entity) {
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
    public SaveWrapper<T> addColumn(boolean condition, String column, Object val) {
        if (condition) {
            addColumn(column);
            addValue(column,val);
        }
        return typedThis;
    }


    public LambdaSaveWrapper<T> lambda() {
        return new LambdaSaveWrapper(this.getEntity(), this.getEntityClass(),
                this.selectColumns, this.paramNameValuePairs, this.expression, this.onWrappers,
                this.lastSql, this.sqlComment, this.sqlFirst, this.tableList, this.index,
                this.sqlKeyword, this.joinClass, this.tableName);
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
    protected SaveWrapper<T> instance() {
        return instance(index, null, null, SharedString.emptyString());
    }

    @Override
    protected SaveWrapper<T> instanceEmpty() {
        return new SaveWrapper<>();
    }

    @Override
    protected SaveWrapper<T> instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName) {
        return new SaveWrapper<>(getEntity(), getEntityClass(), new ArrayList<>(), paramNameValuePairs,
                new MergeSegments(), new ArrayList<>(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString(),
                this.tableList, index, keyWord, joinClass, tableName);
    }


    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return SqlKeyword.INSERT.getKeyword() + " INTO " + getTableName() +
                getSqlSet() + getFromSqlSegment(valuePairs);
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
    public SaveWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.sqlValues = new HashMap<>();
        this.sqlColumns = new LinkedHashSet<>();
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate(...)
     */
    protected SaveWrapper(T entity, Class<T> entityClass, List<ColumnSegment> selectColumns,
                          ParamNameValuePairs paramNameValuePairs, MergeSegments mergeSegments,
                          List<SaveWrapper<T>> onWrappers,
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
