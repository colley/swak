package com.swak.jdbc.conditions;

import com.swak.common.util.StringEscape;
import com.swak.common.util.StringPool;
import com.swak.jdbc.toolkit.JdbcRestrictions;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.ConfigProperties;
import com.swak.jdbc.common.SharedBool;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.enums.SqlLikeMode;
import com.swak.jdbc.metadata.TableList;
import com.swak.jdbc.segments.*;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.swak.jdbc.enums.SqlKeyword.*;
import static java.util.stream.Collectors.joining;

public abstract class WhereStrWrapper<Children extends WhereStrWrapper<Children>> implements CompareStr<Children,String>,
        FuncStr<Children>,Join<Children>,Nested<Children, Children>,SqlSegment{
    /**
     * 主表别名
     */
    @Getter
    protected SharedString alias =new SharedString(ConfigProperties.tableAlias);
    /**
     * 副表别名
     */
    protected SharedString subTableAlias = new SharedString(ConfigProperties.tableAlias);

    @Getter
    protected SharedInteger index = new SharedInteger();

    protected MergeSegments expression;

    protected SharedString lastSql;
    /**
     * SQL注释
     */
    protected SharedString sqlComment;
    /**
     * SQL起始语句
     */
    protected SharedString sqlFirst;


    /**
     * 是否是OnWrapper
     */
    protected SharedBool isOn = new SharedBool(false);


    /**
     * 主表wrapper
     */
    protected SharedBool isMain =  new SharedBool(true);

    /**
     * 表序号
     */
    protected SharedInteger tableIndex = new SharedInteger(1);

    /**
     * 连表实体类 on 条件 func 使用
     */
    @Getter
    protected Class<?> joinClass;

    /**
     * 连表表名
     */
    @Getter
    protected SharedString tableName = new SharedString();

    /**
     * 连表关键字 on 条件 func 使用
     */
    @Getter
    protected SqlKeyword sqlKeyword;

    /**
     * 关联的表
     */
    @Getter
    protected TableList tableList;

    protected abstract   Children getChildren();

    public WhereStrWrapper() {
        super();
        initNeed();
    }

    protected void initNeed() {
        expression = new MergeSegments();
        lastSql = SharedString.emptyString();
        sqlComment = SharedString.emptyString();
        sqlFirst = SharedString.emptyString();
        this.alias = new SharedString(ConfigProperties.tableAlias);
    }

    @Override
    public <V> Children allEqStr(boolean condition, Map<String, V> params, boolean null2IsNull) {
        if (condition && MapUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (StringEscape.checkValNotNull(v)) {
                    eq(k, v);
                } else {
                    if (null2IsNull) {
                        isNull(k);
                    }
                }
            });
        }
        return getChildren();
    }

    @Override
    public <V> Children allEqStr(boolean condition, BiPredicate<String, V> filter, Map<String, V> params, boolean null2IsNull) {
        if (condition && MapUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (filter.test(k, v)) {
                    if (StringEscape.checkValNotNull(v)) {
                        eq(k, v);
                    } else {
                        if (null2IsNull) {
                            isNull(k);
                        }
                    }
                }
            });
        }
        return getChildren();
    }

    @Override
    public Children eq(boolean condition, String column, Object val) {
        return addCondition(condition, column, EQ, val);
    }

    @Override
    public Children ne(boolean condition, String column, Object val) {
        return addCondition(condition, column, NE, val);
    }

    @Override
    public Children gt(boolean condition, String column, Object val) {
        return addCondition(condition, column, GT, val);
    }

    @Override
    public Children ge(boolean condition, String column, Object val) {
        return addCondition(condition, column, GE, val);
    }

    @Override
    public Children lt(boolean condition, String column, Object val) {
        return addCondition(condition, column, LT, val);
    }

    @Override
    public Children le(boolean condition, String column, Object val) {
        return addCondition(condition, column, LE, val);
    }

    @Override
    public Children like(boolean condition, String column, Object val) {
        return likeValue(condition, LIKE, column, val, SqlLikeMode.DEFAULT);
    }

    @Override
    public Children notLike(boolean condition, String column, Object val) {
        return likeValue(condition, NOT_LIKE, column, val, SqlLikeMode.DEFAULT);
    }

    @Override
    public Children likeLeft(boolean condition, String column, Object val) {
        return likeValue(condition, LIKE, column, val, SqlLikeMode.LEFT);
    }

    @Override
    public Children notLikeLeft(boolean condition, String column, Object val) {
        return likeValue(condition, NOT_LIKE, column, val, SqlLikeMode.LEFT);
    }

    @Override
    public Children likeRight(boolean condition, String column, Object val) {
        return likeValue(condition, LIKE, column, val, SqlLikeMode.RIGHT);
    }

    @Override
    public Children notLikeRight(boolean condition, String column, Object val) {
        return likeValue(condition, NOT_LIKE, column, val, SqlLikeMode.RIGHT);
    }

    @Override
    public Children between(boolean condition, String column, Object val1, Object val2) {
        return doIt(condition, JdbcRestrictions.between(columnToString(column), val1, val2));
    }

    @Override
    public Children notBetween(boolean condition, String column, Object val1, Object val2) {
        return doIt(condition, JdbcRestrictions.notBetween(columnToString(column), val1, val2));
    }

    @Override
    public Children isNull(boolean condition, String column) {
        return doIt(condition, JdbcRestrictions.isNull(columnToString(column)));
    }

    @Override
    public Children isNotNull(boolean condition, String column) {
        return doIt(condition, JdbcRestrictions.isNotNull(columnToString(column)));
    }

    @Override
    public Children in(boolean condition, String column, Collection<?> coll) {
        return doIt(condition, JdbcRestrictions.in(columnToString(column), coll));
    }

    @Override
    public Children in(boolean condition, String column, Object... values) {
        return doIt(condition, JdbcRestrictions.in(columnToString(column), values));
    }

    @Override
    public Children notIn(boolean condition, String column, Collection<?> coll) {
        return doIt(condition, JdbcRestrictions.notIn(columnToString(column), coll));
    }

    @Override
    public Children notIn(boolean condition, String column, Object... values) {
        return doIt(condition, JdbcRestrictions.notIn(columnToString(column), values));
    }

    @Override
    public Children inSql(boolean condition, String column, String inValue) {
        return doIt(condition, JdbcRestrictions.addCondition(columnToString(column), IN, String.format("(%s)", inValue)));
    }

    @Override
    public Children notInSql(boolean condition, String column, String inValue) {
        return doIt(condition, JdbcRestrictions.addCondition(columnToString(column), NOT_IN, String.format("(%s)", inValue)));
    }


    @Override
    public Children groupBy(boolean condition, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return getChildren();
        }
        expression.add(GROUP_BY, StringSqlSegment.apply(columns.length == 1 ?
                columnToString(columns[0]) : columnsToString(columns)));
        return getChildren();
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return getChildren();
        }
        for (String column : columns) {
            expression.add(ORDER_BY, OrderSegment.order(columnToString(column), isAsc));
        }
        return getChildren();
    }

    @Override
    public Children having(boolean condition, String sqlHaving, Object... params) {
        expression.add(SqlKeyword.HAVING,formatSqlIfNeed(condition, sqlHaving, params));
        return getChildren();
    }

    public final SqlSegment formatSqlIfNeed(boolean need, String sqlStr, Object... params) {
        if (!need || StringUtils.isBlank(sqlStr)) {
            return null;
        }
        return ApplySqlSegment.apply(sqlStr, params);
    }


    @Override
    public Children func(boolean condition, Consumer<Children> consumer) {
        if (condition) {
            consumer.accept(getChildren());
        }
        return getChildren();
    }

    /**
     * 内部自用
     * <p>拼接 LIKE 以及 值</p>
     */
    protected Children likeValue(boolean condition, SqlKeyword keyword, String column, Object val, SqlLikeMode sqlLike) {
        return doIt(condition, JdbcRestrictions.likeValue(columnToString(column), keyword, val, sqlLike));
    }

    /**
     * 普通查询条件
     *
     * @param condition  是否执行
     * @param column     属性
     * @param sqlKeyword SQL 关键词
     * @param val        条件值
     */
    protected Children addCondition(boolean condition, String column, SqlKeyword sqlKeyword, Object val) {
        return doIt(condition, APPLY, JdbcRestrictions.addCondition(columnToString(column), sqlKeyword, val));
    }


    /**
     * 对sql片段进行组装
     *
     * @param condition   是否执行
     * @param sqlSegments sql片段数组
     * @return Children
     */
    protected Children doIt(boolean condition, SqlSegment... sqlSegments) {
        if (condition) {
            expression.add(sqlSegments);
        }
        return getChildren();
    }

    /**
     * 获取 columnName
     */
    protected String columnToString(String column) {
        if (column.contains(StringPool.DOT) ||
                StringUtils.isEmpty(alias.getValue())) {
            return column;
        }
        return alias.getValue() + StringPool.DOT + column;
    }

    /**
     * 多字段转换为逗号 "," 分割字符串
     *
     * @param columns 多字段
     */
    protected String columnsToString(String... columns) {
        return Arrays.stream(columns).map(this::columnToString).collect(joining(StringPool.COMMA));
    }


    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return expression.getSqlSegment(valuePairs);
    }


    public void clear() {
        expression.clear();
    }

    /**
     * 内部自用
     * <p>NOT 关键词</p>
     */
    protected Children not(boolean condition) {
        return doIt(condition, NOT);
    }

    /**
     * 内部自用
     * <p>拼接 AND</p>
     */
    protected Children and(boolean condition) {
        return doIt(condition, AND);
    }

    protected abstract Children instance();

    /**
     * 多重嵌套查询条件
     *
     * @param condition 查询条件值
     */
    protected Children addNestedCondition(boolean condition, Consumer<Children> consumer) {
        if (condition) {
            final Children instance = instance();
            consumer.accept(instance);
            return doIt(true, APPLY, instance);
        }
        return getChildren();
    }

    public Children addCondition(SqlSegment sqlSegment) {
        expression.add(SqlKeyword.APPLY, sqlSegment);
        return getChildren();
    }

    @Override
    public Children and(boolean condition, Consumer<Children> consumer) {
        return and(condition).addNestedCondition(condition, consumer);
    }

    @Override
    public Children or(boolean condition, Consumer<Children> consumer) {
        return or(condition).addNestedCondition(condition, consumer);
    }

    @Override
    public Children nested(boolean condition, Consumer<Children> consumer) {
        return addNestedCondition(condition, consumer);
    }

    @Override
    public Children not(boolean condition, Consumer<Children> consumer) {
        return not(condition).addNestedCondition(condition, consumer);
    }

    @Override
    public Children or(boolean condition) {
        return doIt(condition, OR);
    }

    @Override
    public Children apply(boolean condition, String applySql, Object... value) {
        return doIt(condition, JdbcRestrictions.apply(formatSql(applySql, value)));
    }

    @Override
    public Children last(boolean condition, String lastSql) {
        if (condition) {
            this.lastSql.setValue(StringPool.SPACE + lastSql);
        }
        return getChildren();
    }

    @Override
    public Children comment(boolean condition, String comment) {
        if (condition) {
            this.sqlComment.setValue(comment);
        }
        return getChildren();
    }

    @Override
    public Children first(boolean condition, String firstSql) {
        if (condition) {
            this.sqlFirst.setValue(firstSql);
        }
        return getChildren();
    }

    @Override
    public Children exists(boolean condition, String existsSql) {
        return doIt(condition, JdbcRestrictions.apply(EXISTS,StringSqlSegment.apply(String.format("(%s)", existsSql))));
    }

    @Override
    public Children notExists(boolean condition, String existsSql) {
        return not(condition).exists(condition, existsSql);
    }

    /**
     * 格式化SQL
     *
     * @param sqlStr SQL语句部分
     * @param params 参数集
     * @return sql
     */
    protected SqlSegment formatSql(String sqlStr, Object... params) {
        return formatSqlIfNeed(true, sqlStr, params);
    }
}
