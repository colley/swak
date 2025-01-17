package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.conditions.*;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.segments.SqlSegment;
import com.swak.jdbc.segments.WhereSegment;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * AbstractChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public abstract   class AbstractChainWrapper<T, R, Children extends AbstractChainWrapper<T, R, Children, Param>, Param extends AbstractLambdaWrapper<T, Param>>
        implements WhereSegment<Children>, QueryJoin<Children, T>, OnCompare<Children>, StringJoin<Children, T>{

    protected final Children typedThis = (Children) this;
    /**
     * 子类所包装的具体 Wrapper 类型
     */
    protected Param wrapperChildren;

    /**
     * 必须的构造函数
     */
    public AbstractChainWrapper() {
    }

    public AbstractLambdaWrapper<T,Param> getWrapper() {
        return wrapperChildren;
    }

    public Children setEntity(T entity) {
        getWrapper().setEntity(entity);
        return typedThis;
    }

    public Children setEntityClass(Class<T> entityClass) {
        getWrapper().setEntityClass(entityClass);
        return typedThis;
    }

    @Override
    public <R, V> Children allEq(boolean condition, Map<SFunction<R, ?>, V> params, boolean null2IsNull) {
        getWrapper().allEq(condition, params, null2IsNull);
        return typedThis;
    }

    @Override
    public <R, V> Children allEq(boolean condition, BiPredicate<SFunction<R, ?>, V> filter, Map<SFunction<R, ?>, V> params, boolean null2IsNull) {
        getWrapper().allEq(condition, filter, params, null2IsNull);
        return typedThis;
    }

    @Override
    public <R> Children eq(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().eq(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children ne(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().ne(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children gt(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().gt(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children ge(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().ge(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children lt(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().lt(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children le(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().le(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children between(boolean condition, String alias, SFunction<R, ?> column, Object val1, Object val2) {
        getWrapper().between(condition, alias, column, val1, val2);
        return typedThis;
    }

    @Override
    public <R> Children notBetween(boolean condition, String alias, SFunction<R, ?> column, Object val1, Object val2) {
        getWrapper().notBetween(condition, alias, column, val1, val2);
        return typedThis;
    }

    @Override
    public <R> Children like(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().like(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children notLike(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().notLike(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children likeLeft(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().likeLeft(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children notLikeLeft(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().notLikeLeft(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children likeRight(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().likeRight(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <R> Children notLikeRight(boolean condition, String alias, SFunction<R, ?> column, Object val) {
        getWrapper().notLikeRight(condition, alias, column, val);
        return typedThis;
    }

    @Override
    public <V> Children allEqStr(boolean condition, Map<String, V> params, boolean null2IsNull) {
        getWrapper().allEqStr(condition, params, null2IsNull);
        return typedThis;
    }

    @Override
    public <V> Children allEqStr(boolean condition, BiPredicate<String, V> filter, Map<String, V> params, boolean null2IsNull) {
        getWrapper().allEqStr(condition, filter, params, null2IsNull);
        return typedThis;
    }

    @Override
    public Children eq(boolean condition, String column, Object val) {
        getWrapper().eq(condition, column, val);
        return typedThis;
    }

    @Override
    public Children ne(boolean condition, String column, Object val) {
        getWrapper().ne(condition, column, val);
        return typedThis;
    }

    @Override
    public Children gt(boolean condition, String column, Object val) {
        getWrapper().gt(condition, column, val);
        return typedThis;
    }

    @Override
    public Children ge(boolean condition, String column, Object val) {
        getWrapper().ge(condition, column, val);
        return typedThis;
    }

    @Override
    public Children lt(boolean condition, String column, Object val) {
        getWrapper().lt(condition, column, val);
        return typedThis;
    }

    @Override
    public Children le(boolean condition, String column, Object val) {
        getWrapper().le(condition, column, val);
        return typedThis;
    }

    @Override
    public Children between(boolean condition, String column, Object val1, Object val2) {
        getWrapper().between(condition, column, val1, val2);
        return typedThis;
    }

    @Override
    public Children notBetween(boolean condition, String column, Object val1, Object val2) {
        getWrapper().notBetween(condition, column, val1, val2);
        return typedThis;
    }

    @Override
    public Children like(boolean condition, String column, Object val) {
        getWrapper().like(condition, column, val);
        return typedThis;
    }

    @Override
    public Children notLike(boolean condition, String column, Object val) {
        getWrapper().notLike(condition, column, val);
        return typedThis;
    }

    @Override
    public Children likeLeft(boolean condition, String column, Object val) {
        getWrapper().likeLeft(condition, column, val);
        return typedThis;
    }

    @Override
    public Children notLikeLeft(boolean condition, String column, Object val) {
        getWrapper().notLikeLeft(condition, column, val);
        return typedThis;
    }

    @Override
    public Children likeRight(boolean condition, String column, Object val) {
        getWrapper().likeRight(condition, column, val);
        return typedThis;
    }

    @Override
    public Children notLikeRight(boolean condition, String column, Object val) {
        getWrapper().notLikeRight(condition, column, val);
        return typedThis;
    }

    @Override
    public <R> Children isNull(boolean condition, SFunction<R, ?> column) {
        getWrapper().isNull(condition, column);
        return typedThis;
    }

    @Override
    public <R> Children isNotNull(boolean condition, SFunction<R, ?> column) {
        getWrapper().isNotNull(condition, column);
        return typedThis;
    }

    @Override
    public <R> Children in(boolean condition, SFunction<R, ?> column, Collection<?> coll) {
        getWrapper().in(condition, column, coll);
        return typedThis;
    }

    @Override
    public <R> Children notIn(boolean condition, SFunction<R, ?> column, Collection<?> coll) {
        getWrapper().notIn(condition, column, coll);
        return typedThis;
    }

    @Override
    public <R> Children inSql(boolean condition, SFunction<R, ?> column, String inValue) {
        getWrapper().inSql(condition, column, inValue);
        return typedThis;
    }

    @Override
    public <R> Children notInSql(boolean condition, SFunction<R, ?> column, String inValue) {
        getWrapper().notInSql(condition, column, inValue);
        return typedThis;
    }

    @Override
    public <R> Children groupBy(boolean condition, SFunction<R, ?>... columns) {
        getWrapper().groupBy(condition, columns);
        return typedThis;
    }

    @Override
    public <R> Children orderBy(boolean condition, boolean isAsc, SFunction<R, ?>... columns) {
        getWrapper().orderBy(condition, isAsc, columns);
        return typedThis;
    }

    @Override
    public Children isNull(boolean condition, String column) {
        getWrapper().isNull(condition, column);
        return typedThis;
    }

    @Override
    public Children isNotNull(boolean condition, String column) {
        getWrapper().isNotNull(condition, column);
        return typedThis;
    }

    @Override
    public Children in(boolean condition, String column, Collection<?> coll) {
        getWrapper().in(condition, column, coll);
        return typedThis;
    }

    @Override
    public Children notIn(boolean condition, String column, Collection<?> coll) {
        getWrapper().notIn(condition, column, coll);
        return typedThis;
    }

    @Override
    public Children inSql(boolean condition, String column, String inValue) {
        getWrapper().inSql(condition, column, inValue);
        return typedThis;
    }

    @Override
    public Children notInSql(boolean condition, String column, String inValue) {
        getWrapper().notInSql(condition, column, inValue);
        return typedThis;
    }

    @Override
    public Children groupBy(boolean condition, String... columns) {
        getWrapper().groupBy(condition, columns);
        return typedThis;
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, String... columns) {
        getWrapper().orderBy(condition, isAsc, columns);
        return typedThis;
    }

    @Override
    public Children having(boolean condition, String sqlHaving, Object... params) {
        getWrapper().having(condition, sqlHaving, params);
        return typedThis;
    }

    @Override
    public Children func(boolean condition, Consumer<Children> consumer) {
        getWrapper().func(condition, (v) -> consumer.accept(typedThis));
        return typedThis;
    }

    @Override
    public Children or(boolean condition) {
        getWrapper().or(condition);
        return typedThis;
    }

    @Override
    public Children apply(boolean condition, String applySql, Object... value) {
        getWrapper().apply(condition, applySql, value);
        return typedThis;
    }

    @Override
    public Children last(boolean condition, String lastSql) {
        getWrapper().last(condition, lastSql);
        return typedThis;
    }

    @Override
    public Children comment(boolean condition, String comment) {
        getWrapper().comment(condition, comment);
        return typedThis;
    }

    @Override
    public Children first(boolean condition, String firstSql) {
        getWrapper().comment(condition, firstSql);
        return typedThis;
    }

    @Override
    public Children exists(boolean condition, String existsSql) {
        getWrapper().exists(condition, existsSql);
        return typedThis;
    }

    @Override
    public Children notExists(boolean condition, String existsSql) {
        getWrapper().notExists(condition, existsSql);
        return typedThis;
    }

    @Override
    public Children addCondition(SqlSegment sqlSegment) {
        getWrapper().addCondition(sqlSegment);
        return typedThis;
    }

    @Override
    public Children and(boolean condition, Consumer<Children> consumer) {
        getWrapper().and(condition, (v) -> consumer.accept(typedThis));
        return typedThis;
    }

    @Override
    public Children or(boolean condition, Consumer<Children> consumer) {
        getWrapper().or(condition, (v) -> consumer.accept(typedThis));
        return typedThis;
    }

    @Override
    public Children nested(boolean condition, Consumer<Children> consumer) {
        getWrapper().nested(condition, (v) -> consumer.accept(typedThis));
        return typedThis;
    }

    @Override
    public Children not(boolean condition, Consumer<Children> consumer) {
        getWrapper().not(condition, (v) -> consumer.accept(typedThis));
        return typedThis;
    }

    @Override
    public <R, S> Children eq(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        getWrapper().eq(condition, alias, column, rightAlias, val);
        return typedThis;
    }

    @Override
    public <R, S> Children ne(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        getWrapper().ne(condition, alias, column, rightAlias, val);
        return typedThis;
    }

    @Override
    public <R, S> Children gt(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        getWrapper().gt(condition, alias, column, rightAlias, val);
        return typedThis;
    }

    @Override
    public <R, S> Children ge(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        getWrapper().ge(condition, alias, column, rightAlias, val);
        return typedThis;
    }

    @Override
    public <R, S> Children lt(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        getWrapper().lt(condition, alias, column, rightAlias, val);
        return typedThis;
    }

    @Override
    public <R, S> Children le(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        getWrapper().le(condition, alias, column, rightAlias, val);
        return typedThis;
    }


    @Override
    public Children join(SqlKeyword keyWord, boolean condition, String joinSql) {
        getWrapper().join(keyWord, condition, joinSql);
        return typedThis;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return getWrapper().getSqlSegment(valuePairs);
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return getWrapper().getSqlKeyword();
    }

    @Override
    public Children where(SqlSegment... sqlSegment) {
        getWrapper().where(sqlSegment);
        return typedThis;
    }

    @Override
    public <T1> Children join(SqlKeyword keyWord, Class<T1> clazz, String alias, BiConsumer<AbstractLambdaWrapper<T, ?>, Children> consumer) {
        getWrapper().join(keyWord,clazz,alias,(v,v1)->consumer.accept(getWrapper(),typedThis));
        return typedThis;
    }
}
