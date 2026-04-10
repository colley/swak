package com.swak.jdbc.conditions.update;

import com.swak.common.util.JacksonUtils;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * UpdateWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
public class UpdateWrapper<T> extends AbstractLambdaWrapper<T, UpdateWrapper<T>>
        implements SwakUpdate<UpdateWrapper<T>, String, T> {

    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private final List<String> sqlSet;

    @Override
    public UpdateWrapper<T> getChildren() {
        return this;
    }

    public UpdateWrapper() {
        // 如果无参构造函数，请注意实体 NULL 情况 SET 必须有否则 SQL 异常
        this((T) null);
    }

    public UpdateWrapper(T entity) {
        this.setEntity(entity);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    public UpdateWrapper<T> setEntity(T entity) {
        super.setEntity(entity);
        TableInfo tableInfo = TableHelper.get(getEntityClass());
        if (Objects.nonNull(tableInfo)) {
            resetColumn();
            from(tableInfo.getTableName());
            Map<String, Object> objectMap = JacksonUtils.convertValue(entity);
            for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                set(fieldInfo.getColumn(),objectMap.get(fieldInfo.getProperty()));
            }
        }
        return this;
    }

    private void resetColumn() {
        sqlSet.clear();
    }


    @Override
    public void setSqlKeyword(SqlKeyword sqlKeyword){
        this.sqlKeyword = sqlKeyword;
    }

    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(StringPool.COMMA, sqlSet);
    }

    @Override
    public UpdateWrapper<T> from(String tableName) {
        super.tableName.setValue(tableName);
        return typedThis;
    }

    @Override
    public UpdateWrapper<T> set(boolean condition, String column, Object val) {
        if (condition) {
            sqlSet.add(String.format("%s=%s", column, formatSql("{0}", val)));
        }
        return typedThis;
    }

    @Override
    public UpdateWrapper<T> setSql(boolean condition, String sql) {
        if (condition && StringUtils.isNotBlank(sql)) {
            sqlSet.add(sql);
        }
        return typedThis;
    }


    public LambdaUpdateWrapper<T> lambda() {
        return new LambdaUpdateWrapper(this.getEntity(), this.getEntityClass(),
                this.selectColumns, this.paramNameValuePairs, this.expression, this.onWrappers,
                this.lastSql, this.sqlComment, this.sqlFirst, this.tableList, this.index,
                this.sqlKeyword, this.joinClass, this.tableName);
    }


    @Override
    public void clear() {
        super.clear();
        sqlSet.clear();
    }


    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected UpdateWrapper<T> instance() {
        return instance(index, null, null, SharedString.emptyString());
    }

    @Override
    protected UpdateWrapper<T> instanceEmpty() {
        return new UpdateWrapper<>();
    }

    @Override
    protected UpdateWrapper<T> instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName) {
        return new UpdateWrapper<>(getEntity(), getEntityClass(), new ArrayList<>(), paramNameValuePairs,
                new MergeSegments(), new ArrayList<>(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString(),
                this.tableList, index, keyWord, joinClass, tableName);
    }


    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        if (SqlKeyword.DELETE.equals(this.sqlKeyword)) {
            return getDeleteSqlSegment(valuePairs);
        }
        String whereSql = super.getSqlSegment(valuePairs);
        return SqlKeyword.UPDATE.getKeyword() +
                getTableName().getValue() + StringPool.SPACE +
                getAlias().getValue() +
                SqlKeyword.SET.getKeyword() +
                getSqlSet() +
                whereSql;
    }

    /**
     * DELETE FROM table_name WHERE
     */
    public String getDeleteSqlSegment(ParamNameValuePairs valuePairs) {
        String whereSql = super.getSqlSegment(valuePairs);
        return SqlKeyword.DELETE.getKeyword() + SqlKeyword.FROM +
                getTableName().getValue() + StringPool.SPACE +
                getAlias().getValue() +
                whereSql;
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate(entity)
     */
    public UpdateWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaUpdate(...)
     */
    protected UpdateWrapper(T entity, Class<T> entityClass, List<ColumnSegment> selectColumns,
                            ParamNameValuePairs paramNameValuePairs, MergeSegments mergeSegments,
                            List<UpdateWrapper<T>> onWrappers,
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
}
