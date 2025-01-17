package com.swak.jdbc.conditions;


import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.metadata.TableFieldInfo;
import com.swak.jdbc.metadata.TableInfo;
import com.swak.jdbc.parser.HsSqlSourceBuilder;
import com.swak.jdbc.parser.SwakBoundSql;
import com.swak.jdbc.toolkit.TableHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.swak.jdbc.enums.SqlKeyword.*;

public abstract class AbstractWrapper<T, Children extends AbstractWrapper<T, Children>>
        extends AbstractBaseWrapper<T, Children> implements OnCompare<Children>, StringJoin<Children, T> {

    public AbstractWrapper() {
        super();
        this.initNeed();
    }

    /**
     * 推荐使用此构造方法
     */
    public AbstractWrapper(Class<T> clazz) {
        this();
        setEntityClass(clazz);
        tableList.setRootClass(clazz);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        StringBuilder builder = new StringBuilder();
        builder.append(getJoinFromSqlSegment(valuePairs));
        String whereSql = super.getSqlSegment(valuePairs);
        if (StringUtils.isNotEmpty(whereSql)) {
            builder.append(whereSql);
        }
        return builder.toString();
    }

    protected String getJoinFromSqlSegment(ParamNameValuePairs valuePairs) {
        if (StringUtils.isBlank(getFrom().getValue()) &&
                CollectionUtils.isEmpty(getOnWrappers())) {
            return StringPool.EMPTY;
        }
        if (StringUtils.isBlank(getFrom().getValue())) {
            StringBuilder value = new StringBuilder();
            for (Children wrapper : getOnWrappers()) {
                if (StringUtils.isBlank(wrapper.getFrom().getValue())) {
                    value.append(StringPool.SPACE)
                            .append(wrapper.getSqlKeyword())
                            .append(StringPool.SPACE)
                            .append(wrapper.getTableName())
                            .append(StringPool.SPACE)
                            .append(wrapper.hasAlias.isTrue() ? wrapper.alias.getValue() : (wrapper.alias.getValue() + wrapper.getIndex().getValue()))
                            .append(ON.getKeyword())
                            .append(wrapper.expression.getSqlSegment(valuePairs));
                } else {
                    value.append(StringPool.SPACE)
                            .append(wrapper.getSqlKeyword())
                            .append(StringPool.SPACE)
                            .append(wrapper.getFrom().getValue())
                            .append(StringPool.SPACE);
                }
            }
            getFrom().setValue(value.toString());
        }
        return getFrom().getValue();
    }

    /**
     * 构造方法
     *
     * @param entity 主表实体
     */
    public AbstractWrapper(T entity) {
        this();
        setEntity(entity);
        if (entity != null) {
            tableList.setRootClass(entity.getClass());
        }
    }

    /* ************************* on语句重载 *************************** */
    @Override
    public <R, S> Children eq(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, EQ, rightAlias, val);
    }

    @Override
    public <R, S> Children ne(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, NE, rightAlias, val);
    }

    @Override
    public <R, S> Children gt(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, GT, rightAlias, val);
    }

    @Override
    public <R, S> Children ge(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, GE, rightAlias, val);
    }

    @Override
    public <R, S> Children lt(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, LT, rightAlias, val);
    }

    @Override
    public <R, S> Children le(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, LE, rightAlias, val);
    }
    /* ****************************************** **/

    @Override
    public Children join(SqlKeyword keyWord, boolean condition, String joinSql) {
        if (condition) {
            Children wrapper = instanceEmpty();
            wrapper.from.setValue(joinSql);
            wrapper.sqlKeyword = keyWord;
            onWrappers.add(wrapper);
        }
        return getChildren();
    }


    protected abstract Children instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName);

    protected abstract Children instanceEmpty();

    @Override
    protected void initNeed() {
        super.initNeed();
    }


    protected String toColumnSqlString(ParamNameValuePairs paramNameValuePairs) {
        if (CollectionUtils.isNotEmpty(this.selectColumns)) {
            List<String> stringSelectColumn = this.selectColumns.stream().map(s ->
                            s.getSqlSegment(paramNameValuePairs))
                    .collect(Collectors.toList());
            return IbsStringHelper.join(",", stringSelectColumn.iterator());
        }
        Class<T> entityClass = getEntityClass();
        if (Objects.nonNull(entityClass)) {
            TableInfo tableInfo = TableHelper.get(entityClass);
            if (Objects.nonNull(tableInfo)) {
                List<String> stringSelectColumn = tableInfo.getFieldList().stream().map(TableFieldInfo::getColumn).collect(Collectors.toList());
                return IbsStringHelper.join(",", stringSelectColumn.iterator());
            }
        }
        return StringPool.EMPTY;
    }

    public String getSqlSegment() {
        return getSqlFirst() + this.getSqlSegment(getParamNameValuePairs());
    }

    @Override
    public SwakBoundSql getBoundSql() {
        HsSqlSourceBuilder sqlSourceBuilder = new HsSqlSourceBuilder();
        return sqlSourceBuilder.parse(getSqlSegment())
                .getBoundSql(getParamNameValuePairs().getParameter());
    }

    @Override
    public String getStaticSql() {
        HsSqlSourceBuilder sqlSourceBuilder = new HsSqlSourceBuilder();
        SwakBoundSql boundSql = sqlSourceBuilder.parseProviderSql(getSqlSegment())
                .getBoundSql(getParamNameValuePairs().getParameter());
        return boundSql.getSql();
    }
}
