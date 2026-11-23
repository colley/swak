package com.swak.test.jdbc;

import com.swak.jdbc.metadata.SqlInfo;
import com.swak.jdbc.parser.ISqlParser;
import com.swak.jdbc.parser.JsqlParserCountOptimize;
import com.swak.jdbc.parser.SqlParserUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 分页 count SQL 的改写回归。
 * <p>
 * pagehelper 6.1.0 仍锁 jsqlparser 4.7，而 4.7 删掉了 {@code SelectExpressionItem}
 * （升级时改成了 {@code SelectItem.from()}）。这里的用例把改写结果按字符串钉死，
 * 后续再动 jsqlparser / pagehelper 版本时能立刻发现 SQL 形状变化。
 */
class PaginationCountSqlTest {

    private final ISqlParser parser = new JsqlParserCountOptimize();

    @Test
    @DisplayName("普通查询：改写为 COUNT(1)，并去掉 ORDER BY")
    void stripOrderBy() {
        SqlInfo info = parser.parser("select id, name from t_user where name = ? and age > ? order by name desc");

        assertThat(info.getSql()).isEqualTo("SELECT COUNT(1) FROM t_user WHERE name = ? AND age > ?");
        assertThat(info.isOrderBy()).as("ORDER BY 已被移除，分页查询无需再判断排序").isFalse();
    }

    @Test
    @DisplayName("无 where / order by 的简单查询")
    void simpleSelect() {
        assertThat(parser.parser("select * from t_user").getSql())
                .isEqualTo("SELECT COUNT(1) FROM t_user");
    }

    @Test
    @DisplayName("DISTINCT 不能直接改 COUNT，需包一层子查询")
    void distinctWrapped() {
        SqlInfo info = parser.parser("select distinct name from t_user");

        assertThat(info.getSql()).isEqualTo("SELECT COUNT(*) FROM (SELECT DISTINCT name FROM t_user) TOTAL");
        assertThat(info.isOrderBy()).isTrue();
    }

    @Test
    @DisplayName("GROUP BY 保留原 SQL 并包一层子查询")
    void groupByWrapped() {
        assertThat(parser.parser("select name, count(1) from t_user group by name order by name").getSql())
                .isEqualTo("SELECT COUNT(*) FROM (SELECT name, count(1) FROM t_user GROUP BY name ORDER BY name) TOTAL");
    }

    @Test
    @DisplayName("LEFT JOIN 不做裁剪（optimizeJoin 目前写死 false），join 原样保留")
    void joinIsKept() {
        assertThat(parser.parser("select * from t_user a left join t_role b on a.role_id = b.id where a.id = ?").getSql())
                .isEqualTo("SELECT COUNT(1) FROM t_user a LEFT JOIN t_role b ON a.role_id = b.id WHERE a.id = ?");
    }

    @Test
    @DisplayName("FROM 子查询原样保留")
    void subSelectInFrom() {
        assertThat(parser.parser("select * from (select id from t_user) x").getSql())
                .isEqualTo("SELECT COUNT(1) FROM (SELECT id FROM t_user) x");
    }

    @Test
    @DisplayName("无法解析的 SQL 回退为包装形式，不抛异常")
    void brokenSqlFallsBack() {
        assertThat(parser.parser("select from").getSql())
                .isEqualTo("SELECT COUNT(*) FROM (select from) TOTAL");
    }

    @Test
    @DisplayName("关闭 count 优化时直接包一层子查询")
    void optimizeDisabled() {
        String sql = "select id from t_user order by id";

        assertThat(SqlParserUtils.getOptimizeCountSql(false, sql).getSql())
                .isEqualTo("SELECT COUNT(*) FROM (select id from t_user order by id) TOTAL");
        assertThat(SqlParserUtils.getOptimizeCountSql(true, sql).getSql())
                .isEqualTo("SELECT COUNT(1) FROM t_user");
    }
}
