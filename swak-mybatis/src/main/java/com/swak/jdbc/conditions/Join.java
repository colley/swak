
package  com.swak.jdbc.conditions;

import java.io.Serializable;

/**
 * 查询条件封装
 * <p>拼接</p>
 */
public interface Join<Children> extends Serializable {

    /**
     * ignore
     */
    default Children or() {
        return or(true);
    }

    /**
     * 拼接 OR
     *
     * @param condition 执行条件
     * @return children
     */
    Children or(boolean condition);

    /**
     * ignore
     */
    default Children apply(String applySql, Object... value) {
        return apply(true, applySql, value);
    }

    /**
     * 拼接 sql
     * <p>!! 会有 sql 注入风险 !!</p>
     * <p>例1: apply("id = 1")</p>
     * <p>例2: apply("date_format(dateColumn,'%Y-%m-%d') = '2008-08-08'")</p>
     * <p>例3: apply("date_format(dateColumn,'%Y-%m-%d') = {0}", LocalDate.now())</p>
     *
     * @param condition 执行条件
     * @return children
     */
    Children apply(boolean condition, String applySql, Object... value);

    /**
     * ignore
     */
    default Children last(String lastSql) {
        return last(true, lastSql);
    }

    /**
     * 无视优化规则直接拼接到 sql 的最后(有sql注入的风险,请谨慎使用)
     * <p>例: last("limit 1")</p>
     * <p>注意只能调用一次,多次调用以最后一次为准</p>
     *
     * @param condition 执行条件
     * @param lastSql   sql语句
     * @return children
     */
    Children last(boolean condition, String lastSql);

    /**
     * ignore
     */
    default Children comment(String comment) {
        return comment(true, comment);
    }

    /**
     * sql 注释(会拼接在 sql 的最后面)
     *
     * @param condition 执行条件
     * @param comment   sql注释
     * @return children
     */
    Children comment(boolean condition, String comment);

    /**
     * ignore
     */
    default Children first(String firstSql) {
        return first(true, firstSql);
    }

    /**
     * sql 起始句（会拼接在SQL语句的起始处）
     *
     * @param condition 执行条件
     * @param firstSql  起始语句
     * @return children
     * @since 3.3.1
     */
    Children first(boolean condition, String firstSql);

    /**
     * ignore
     */
    default Children exists(String existsSql) {
        return exists(true, existsSql);
    }

    /**
     * 拼接 EXISTS ( sql语句 )
     * <p>!! sql 注入方法 !!</p>
     * <p>例: exists("select id from table where age = 1")</p>
     *
     * @param condition 执行条件
     * @param existsSql sql语句
     * @return children
     */
    Children exists(boolean condition, String existsSql);

    /**
     * ignore
     */
    default Children notExists(String existsSql) {
        return notExists(true, existsSql);
    }

    /**
     * 拼接 NOT EXISTS ( sql语句 )
     * <p>!! sql 注入方法 !!</p>
     * <p>例: notExists("select id from table where age = 1")</p>
     *
     * @param condition 执行条件
     * @param existsSql sql语句
     * @return children
     */
    Children notExists(boolean condition, String existsSql);
}
