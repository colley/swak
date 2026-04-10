package com.swak.jdbc.conditions;

import com.swak.common.util.StringEscape;
import com.swak.common.util.StringPool;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.toolkit.JdbcRestrictions;
import com.swak.jdbc.enums.PrefixEnum;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.enums.SqlLikeMode;
import com.swak.jdbc.metadata.SelectCache;
import com.swak.jdbc.segments.OrderSegment;
import com.swak.jdbc.segments.SqlSegment;
import com.swak.jdbc.segments.StringSqlSegment;
import com.swak.jdbc.toolkit.LambdaUtils;
import com.swak.jdbc.toolkit.support.ColumnCache;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;

import static com.swak.jdbc.enums.SqlKeyword.*;
import static java.util.stream.Collectors.joining;

public abstract class WhereWrapper<Children extends WhereWrapper<Children>> extends WhereStrWrapper<Children> implements Compare<Children>, Func<Children> {

    public WhereWrapper() {
        super();
    }

    protected <R> String columnToString(SFunction<R, ?> column) {
        String lastColumn = this.columnToString(column, true);
        if (lastColumn.contains(com.swak.common.util.StringPool.DOT) ||
                StringUtils.isEmpty(alias.getValue())) {
            return lastColumn;
        }
        return alias.getValue() + StringPool.DOT + lastColumn;
    }

    protected <R> String columnsToString(SFunction<R, ?>... columns) {
        return Arrays.stream(columns).map(this::columnToString).collect(joining(StringPool.COMMA));
    }

    protected <R> String columnToString(SFunction<R, ?> column, boolean onlyColumn) {
        return getCache(column).getColumn();
    }

    protected SelectCache getCache(SFunction<?, ?> fn) {
        Class<?> aClass = LambdaUtils.getEntityClass(fn);
        Map<String, SelectCache> cacheMap = ColumnCache.getMapField(aClass);
        return cacheMap.get(LambdaUtils.getName(fn));
    }

    @Override
    public <R, V> Children allEq(boolean condition, Map<SFunction<R, ?>, V> params, boolean null2IsNull) {
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
    public <R, V> Children allEq(boolean condition, BiPredicate<SFunction<R, ?>, V> filter, Map<SFunction<R, ?>, V> params, boolean null2IsNull) {
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
    public <R> Children eq(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return addCondition(condition, alias,column, EQ, val);
    }


    @Override
    public <R> Children ne(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return addCondition(condition,alias, column, NE, val);
    }


    @Override
    public <R> Children gt(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return addCondition(condition, alias,column, GT, val);
    }

    @Override
    public <R> Children ge(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return addCondition(condition, alias,column, GE, val);
    }


    @Override
    public <R> Children lt(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return addCondition(condition,alias, column, LT, val);
    }


    @Override
    public <R> Children le(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return addCondition(condition,alias, column, LE, val);
    }


    @Override
    public <R> Children like(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return likeValue(condition, LIKE, column, val, SqlLikeMode.DEFAULT);
    }

    @Override
    public <R> Children notLike(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return likeValue(condition, NOT_LIKE, alias,column,val, SqlLikeMode.DEFAULT);
    }


    @Override
    public <R> Children likeLeft(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return likeValue(condition, LIKE,alias, column, val, SqlLikeMode.LEFT);
    }


    @Override
    public <R> Children notLikeLeft(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return likeValue(condition, NOT_LIKE, alias,column, val, SqlLikeMode.LEFT);
    }


    @Override
    public <R> Children likeRight(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return likeValue(condition, LIKE,alias, column, val, SqlLikeMode.RIGHT);
    }

    @Override
    public <R> Children notLikeRight(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        return likeValue(condition, NOT_LIKE,alias, column, val, SqlLikeMode.RIGHT);
    }

    @Override
    public <R> Children between(boolean condition, String alias, SFunction<R, ?> column, Object val1, Object val2) {
        return doIt(condition, JdbcRestrictions.between(columnToString(index.getValue(), alias, column),val1, val2));
    }

    @Override
    public <R> Children notBetween(boolean condition, String alias, SFunction<R, ?> column, Object val1, Object val2) {
        return doIt(condition, JdbcRestrictions.notBetween(columnToString(index.getValue(), alias, column),val1, val2));
    }

    @Override
    public <R> Children isNull(boolean condition, SFunction<R, ?> column) {
        return doIt(condition, JdbcRestrictions.isNull(columnToString(column)));
    }

    @Override
    public <R> Children isNotNull(boolean condition, SFunction<R, ?> column) {
        return doIt(condition, JdbcRestrictions.isNotNull(columnToString(column)));
    }

    @Override
    public <R> Children in(boolean condition, SFunction<R, ?> column, Collection<?> coll) {
        return doIt(condition, JdbcRestrictions.in(columnToString(column), coll));
    }

    @Override
    public <R> Children in(boolean condition, SFunction<R, ?> column, Object... values) {
        return doIt(condition, JdbcRestrictions.in(columnToString(column), values));
    }

    @Override
    public <R> Children notIn(boolean condition, SFunction<R, ?> column, Collection<?> coll) {
        return doIt(condition, JdbcRestrictions.notIn(columnToString(column), coll));
    }

    @Override
    public <R> Children notIn(boolean condition, SFunction<R, ?> column, Object... values) {
        return doIt(condition, JdbcRestrictions.notIn(columnToString(column), values));
    }

    @Override
    public <R> Children inSql(boolean condition, SFunction<R, ?> column, String inValue) {
        return doIt(condition, JdbcRestrictions.addCondition(columnToString(column), IN, String.format("(%s)", inValue)));
    }

    @Override
    public <R> Children notInSql(boolean condition, SFunction<R, ?> column, String inValue) {
        return doIt(condition, JdbcRestrictions.addCondition(columnToString(column), NOT_IN, String.format("(%s)", inValue)));
    }


    @Override
    public <R> Children groupBy(boolean condition, SFunction<R, ?>... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return getChildren();
        }
        expression.add(GROUP_BY, StringSqlSegment.apply(columns.length == 1 ?
                columnToString(columns[0]) : columnsToString(columns)));
        return getChildren();
    }

    @Override
    public <R> Children orderBy(boolean condition, boolean isAsc, SFunction<R, ?>... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return getChildren();
        }
        for (SFunction<R, ?> column : columns) {
            expression.add(ORDER_BY, OrderSegment.order(columnToString(column), isAsc));
        }
        return getChildren();
    }

    /**
     * 内部自用
     * <p>拼接 LIKE 以及 值</p>
     */
    protected <R> Children likeValue(boolean condition, SqlKeyword keyword, SFunction<R, ?> column, Object val, SqlLikeMode sqlLike) {
        return doIt(condition, JdbcRestrictions.likeValue(columnToString(column), keyword, val, sqlLike));
    }

    /**
     * 内部自用
     * <p>拼接 LIKE 以及 值</p>
     */
    protected <X> Children likeValue(boolean condition, SqlKeyword keyword, String alias, SFunction<X, ?> column, Object val, SqlLikeMode sqlLike) {
        return doIt(condition, JdbcRestrictions.likeValue(columnToString(index.getValue(), alias, column), keyword,val, sqlLike));
    }

    protected Children likeValue(boolean condition, SqlKeyword keyword, String column, Object val, SqlLikeMode sqlLike) {
        return doIt(condition,JdbcRestrictions.likeValue(column, keyword,val, sqlLike));
    }

    /**
     * 普通查询条件
     *
     * @param condition  是否执行
     * @param column     属性
     * @param sqlKeyword SQL 关键词
     * @param val        条件值
     */
    protected <R> Children addCondition(boolean condition, SFunction<R, ?> column, SqlKeyword sqlKeyword, Object val) {
        return doIt(condition, APPLY, JdbcRestrictions.addCondition(columnToString(column), sqlKeyword, val));
    }

    protected <X> Children addCondition(boolean condition, String alias, SFunction<X, ?> column, SqlKeyword sqlKeyword, Object val) {
        return doIt(condition, APPLY, JdbcRestrictions.addCondition(columnToString(index.getValue(), alias, column), sqlKeyword, val));
    }

    protected <X, S> Children addCondition(boolean condition, String alias, SFunction<X, ?> column,
                                           SqlKeyword sqlKeyword, String rightAlias, SFunction<S, ?> val) {
        Class<X> c = LambdaUtils.getEntityClass(column);
        Class<S> v = LambdaUtils.getEntityClass(val);
        return doIt(condition, columnToSqlSegment(index.getValue(), alias, column), sqlKeyword,
                isOn.isTrue() ? columnToSqlSegmentS(index.getValue(), rightAlias, val, v == c && v == joinClass) :
                        columnToSqlSegmentS(index.getValue(), rightAlias, val, v == c));
    }

    protected final <X> SqlSegment columnToSqlSegment(Integer index, String alias, SFunction<X, ?> column) {
        return StringSqlSegment.apply(columnToString(index, alias, column, false, isOn.isTrue() ? PrefixEnum.ON_FIRST : PrefixEnum.CD_FIRST));
    }

    protected final <X> String columnToString(Integer index, String alias, SFunction<X, ?> column) {
        return columnToString(index, alias, column, false, isOn.isTrue() ? PrefixEnum.ON_FIRST : PrefixEnum.CD_FIRST);
    }

    protected <X> String columnToString(Integer index, String alias, X column, boolean isJoin, PrefixEnum prefixEnum) {
        return columnToString(index, alias, (SFunction<?, ?>) column, isJoin, prefixEnum);
    }

    protected final <X> String columnsToString(Integer index, PrefixEnum prefixEnum, String alias, X... columns) {
        return Arrays.stream(columns).map(i -> columnToString(index, alias, (SFunction<?, ?>) i, false, prefixEnum)).collect(joining(StringPool.COMMA));
    }

    protected String columnToString(Integer index, String alias, SFunction<?, ?> column, boolean isJoin, PrefixEnum prefixEnum) {
        Class<?> entityClass = LambdaUtils.getEntityClass(column);
        return (alias == null ? getDefault(index, entityClass, isJoin, prefixEnum) : alias) + StringPool.DOT + getCache(column).getColumn();
    }


    protected final <X> SqlSegment columnToSqlSegmentS(Integer index, String alias, SFunction<X, ?> column, boolean isJoin) {
        PrefixEnum prefixEnum;
        if (isMain.getValue()) {
            prefixEnum = isOn.isTrue() ? PrefixEnum.ON_SECOND /* 理论上不可能有这种情况 */ : PrefixEnum.CD_SECOND;
        } else {
            prefixEnum = isOn.isTrue() ? PrefixEnum.ON_SECOND : PrefixEnum.CD_ON_SECOND;
        }
        return StringSqlSegment.apply(columnToString(index, alias, column, isJoin, prefixEnum));
    }

    /**
     * 返回前缀
     */
    protected String getDefault(Integer index, Class<?> clazz, boolean isJoin, PrefixEnum prefixEnum) {
        if (prefixEnum == PrefixEnum.ON_FIRST) {
            return tableList.getPrefix(index, clazz, false);
        } else if (prefixEnum == PrefixEnum.ON_SECOND) {
            return isJoin ? tableList.getPrefixOther(index, clazz) : tableList.getPrefix(index, clazz, false);
        } else if (prefixEnum == PrefixEnum.CD_FIRST) {
            return tableList.getPrefix(index, clazz, false);
        } else if (prefixEnum == PrefixEnum.CD_SECOND) {
            return isJoin ? tableList.getPrefixOther(index, clazz) :
                    tableList.getPrefix(index, clazz, false);
        } else if (prefixEnum == PrefixEnum.CD_ON_FIRST) {
            return tableList.getPrefix(index, clazz, false);
        } else if (prefixEnum == PrefixEnum.CD_ON_SECOND) {
            return isJoin ? tableList.getPrefixOther(index, clazz) :
                    tableList.getPrefix(index, clazz, false);
        } else {
            return tableList.getAlias();
        }
    }
}
