package com.swak.jdbc.conditions.update;

import com.swak.common.exception.SwakAssert;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * LambdaUpdateWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class LambdaUpdateWrapper<T> extends AbstractLambdaWrapper<T, LambdaUpdateWrapper<T>>
        implements SwakUpdate<LambdaUpdateWrapper<T>, SFunction<T, ?>, T> {

    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private final List<String> sqlSet;

    @Override
    protected LambdaUpdateWrapper<T> getChildren() {
        return this;
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate()
     */
    public LambdaUpdateWrapper() {
        // 如果无参构造函数，请注意实体 NULL 情况 SET 必须有否则 SQL 异常
        this((T) null);
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate(entity)
     */
    public LambdaUpdateWrapper(T entity) {
        this.sqlSet = new ArrayList<>();
        super.initNeed();
        this.setEntity(entity);
    }

    @Override
    public LambdaUpdateWrapper<T> setEntity(T entity) {
        super.setEntity(entity);
        TableInfo tableInfo = TableHelper.get(getEntityClass());
        if (Objects.nonNull(tableInfo)) {
            resetColumn();
            from(tableInfo.getTableName());
            Map<String, Object> objectMap = JacksonUtils.convertValue(entity);
            for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                setByFieldStrategy(fieldInfo, objectMap);
            }
        }
        return this;
    }

    private void setByFieldStrategy(TableFieldInfo fieldInfo, Map<String, Object> objectMap) {
        FieldStrategy fieldStrategy = Optional.ofNullable(fieldInfo.getInsertStrategy()).orElse(FieldStrategy.DEFAULT);
        Object value = objectMap.get(fieldInfo.getProperty());
        switch (fieldStrategy) {
            case DEFAULT:
            case NOT_NULL: {
                if (Objects.nonNull(value)) {
                    setInnerNeed(true, fieldInfo.getColumn(), value);
                }
                break;
            }
            case IGNORED: {
                setInnerNeed(true, fieldInfo.getColumn(), value);
                break;
            }
            case NEVER:
            default: {
                break;
            }
        }
    }

    private void resetColumn() {
        sqlSet.clear();
    }

    @Override
    public LambdaUpdateWrapper<T> from(String tableName) {
        super.tableName.setValue(tableName);
        return this;
    }

    @Override
    public LambdaUpdateWrapper<T> setEntityClass(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        if (Objects.nonNull(entityClass)) {
            TableInfo tableInfo = TableHelper.get(entityClass);
            if (Objects.nonNull(tableInfo)) {
                from(tableInfo.getTableName());
            }
        }
        return this;
    }


    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected LambdaUpdateWrapper<T> instance() {
        return instance(index, null, null, SharedString.emptyString());
    }

    @Override
    protected LambdaUpdateWrapper<T> instanceEmpty() {
        return new LambdaUpdateWrapper<>();
    }

    @Override
    protected LambdaUpdateWrapper<T> instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName) {
        return new LambdaUpdateWrapper<>(getEntity(), getEntityClass(), new ArrayList<>(), paramNameValuePairs,
                new MergeSegments(), new ArrayList<>(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString(),
                this.tableList, index, keyWord, joinClass, tableName);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        SwakAssert.notBlank(getTableName().getValue(), "table Name must not blank");
        String whereSql = super.getSqlSegment(valuePairs);
        return SqlKeyword.UPDATE.getKeyword() +
                getTableName().getValue() + StringPool.SPACE +
                getAlias().getValue() +
                SqlKeyword.SET.getKeyword() +
                getSqlSet() +
                whereSql;
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate(entity)
     */
    public LambdaUpdateWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate(...)
     */
    protected LambdaUpdateWrapper(T entity, Class<T> entityClass, List<ColumnSegment> selectColumns,
                                  ParamNameValuePairs paramNameValuePairs, MergeSegments mergeSegments,
                                  List<LambdaUpdateWrapper<T>> onWrappers,
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
        this.sqlSet = new ArrayList<>();
    }

    @Override
    public LambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        setInnerNeed(condition, columnToString(column, true), val);
        return typedThis;
    }

    public LambdaUpdateWrapper<T> setInnerNeed(boolean condition, String column, Object val) {
        if (condition) {
            sqlSet.add(String.format("%s=%s", column, formatSqlStr("{0}", val)));
        }
        return typedThis;
    }

    protected String formatSqlStr(String sqlStr, Object... params) {
        if (StringUtils.isBlank(sqlStr)) {
            return null;
        }
        if (ArrayUtils.isNotEmpty(params)) {
            for (int i = 0; i < params.length; ++i) {
                String genParamName = paramNameValuePairs.addParameter(Constants.WRAPPER_PARAM, params[i]);
                sqlStr = sqlStr.replace(String.format("{%s}", i),
                        IbsStringHelper.repeatParamFormat(genParamName));
            }
        }
        return sqlStr;
    }

    @Override
    public LambdaUpdateWrapper<T> setSql(boolean condition, String sql) {
        if (condition && StringUtils.isNotBlank(sql)) {
            sqlSet.add(sql);
        }
        return typedThis;
    }

    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(StringPool.COMMA, sqlSet);
    }

    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }
}
