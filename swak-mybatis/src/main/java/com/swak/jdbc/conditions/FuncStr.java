
package com.swak.jdbc.conditions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;


public interface FuncStr<Children> extends Serializable {

    /**
     * ignore
     */
    default Children isNull(String column) {
        return isNull(true, column);
    }

    /**
     * 字段 IS NULL
     * <p>例: isNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    Children isNull(boolean condition, String column);

    /**
     * ignore
     */
    default Children isNotNull(String column) {
        return isNotNull(true, column);
    }

    /**
     * 字段 IS NOT NULL
     * <p>例: isNotNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    Children isNotNull(boolean condition, String column);

    /**
     * ignore
     */
    default Children in(String column, Collection<?> coll) {
        return in(true, column, coll);
    }

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 如果集合为 empty 则不会进行 sql 拼接 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children in(boolean condition, String column, Collection<?> coll);

    /**
     * ignore
     */
    default Children in(String column, Object... values) {
        return in(true, column, values);
    }

    /**
     * 字段 IN (v0, v1, ...)
     * <p>例: in("id", 1, 2, 3, 4, 5)</p>
     *
     * <li> 如果动态数组为 empty 则不会进行 sql 拼接 </li>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    default Children in(boolean condition, String column, Object... values) {
        return in(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    /**
     * ignore
     */
    default Children notIn(String column, Collection<?> coll) {
        return notIn(true, column, coll);
    }

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children notIn(boolean condition, String column, Collection<?> coll);

    /**
     * ignore
     */
    default Children notIn(String column, Object... value) {
        return notIn(true, column, value);
    }

    /**
     * 字段 NOT IN (v0, v1, ...)
     * <p>例: notIn("id", 1, 2, 3, 4, 5)</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    default Children notIn(boolean condition, String column, Object... values) {
        return notIn(condition, column, Arrays.stream(Optional.ofNullable(values).orElseGet(() -> new Object[]{}))
                .collect(toList()));
    }

    /**
     * ignore
     */
    default Children inSql(String column, String inValue) {
        return inSql(true, column, inValue);
    }

    /**
     * 字段 IN ( sql语句 )
     * <p>!! sql 注入方式的 in 方法 !!</p>
     * <p>例1: inSql("id", "1, 2, 3, 4, 5, 6")</p>
     * <p>例2: inSql("id", "select id from table where id &lt; 3")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param inValue   sql语句
     * @return children
     */
    Children inSql(boolean condition, String column, String inValue);

    /**
     * ignore
     */
    default Children notInSql(String column, String inValue) {
        return notInSql(true, column, inValue);
    }

    /**
     * 字段 NOT IN ( sql语句 )
     * <p>!! sql 注入方式的 not in 方法 !!</p>
     * <p>例1: notInSql("id", "1, 2, 3, 4, 5, 6")</p>
     * <p>例2: notInSql("id", "select id from table where id &lt; 3")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param inValue   sql语句 ---&gt; 1,2,3,4,5,6 或者 select id from table where id &lt; 3
     * @return children
     */
    Children notInSql(boolean condition, String column, String inValue);

    /**
     * ignore
     */
    default Children groupBy(String column) {
        return groupBy(true, column);
    }

    /**
     * ignore
     */
    default Children groupBy(String... columns) {
        return groupBy(true, columns);
    }

    /**
     * 分组：GStringOUP BY 字段, ...
     * <p>例: groupBy("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return children
     */
    Children groupBy(boolean condition, String... columns);

    /**
     * ignore
     */
    default Children orderByAsc(String column) {
        return orderByAsc(true, column);
    }

    /**
     * ignore
     */
    default Children orderByAsc(String... columns) {
        return orderByAsc(true, columns);
    }

    /**
     * 排序：OStringDEString BY 字段, ... ASC
     * <p>例: orderByAsc("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return children
     */
    default Children orderByAsc(boolean condition, String... columns) {
        return orderBy(condition, true, columns);
    }

    /**
     * ignore
     */
    default Children orderByDesc(String column) {
        return orderByDesc(true, column);
    }

    /**
     * ignore
     */
    default Children orderByDesc(String... columns) {
        return orderByDesc(true, columns);
    }

    /**
     * 排序：OStringDEString BY 字段, ... DESC
     * <p>例: orderByDesc("id", "name")</p>
     *
     * @param condition 执行条件
     * @param columns   字段数组
     * @return children
     */
    default Children orderByDesc(boolean condition, String... columns) {
        return orderBy(condition, false, columns);
    }

    /**
     * 排序：OStringDEString BY 字段, ...
     * <p>例: orderBy(true, "id", "name")</p>
     *
     * @param condition 执行条件
     * @param isAsc     是否是 ASC 排序
     * @param columns   字段数组
     * @return children
     */
    Children orderBy(boolean condition, boolean isAsc, String... columns);

    /**
     * ignore
     */
    default Children having(String sqlHaving, Object... params) {
        return having(true, sqlHaving, params);
    }

    /**
     * HAVING ( sql语句 )
     * <p>例1: having("sum(age) &gt; 10")</p>
     * <p>例2: having("sum(age) &gt; {0}", 10)</p>
     *
     * @param condition 执行条件
     * @param sqlHaving sql 语句
     * @param params    参数数组
     * @return children
     */
    Children having(boolean condition, String sqlHaving, Object... params);

    /**
     * ignore
     */
    default Children func(Consumer<Children> consumer) {
        return func(true, consumer);
    }

    /**
     * 消费函数
     *
     * @param consumer 消费函数
     * @return children
     * @since 3.3.1
     */
    Children func(boolean condition, Consumer<Children> consumer);
}
