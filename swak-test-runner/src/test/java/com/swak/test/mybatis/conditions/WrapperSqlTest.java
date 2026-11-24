package com.swak.test.mybatis.conditions;

import com.swak.jdbc.conditions.query.QueryWrapper;
import com.swak.jdbc.conditions.update.SaveWrapper;
import com.swak.jdbc.conditions.update.UpdateWrapper;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.test.mybatis.fixture.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 条件构造器生成的 SQL 回归。
 * <p>
 * 重点锁定三类曾经出错的行为：
 * <ol>
 *     <li>单段条件（between / in / isNull / apply / inSql）不能丢，且要补 AND；</li>
 *     <li>嵌套条件只拼子 wrapper 的条件部分，不能把整条 SELECT 塞进来；</li>
 *     <li>group by / set 不能把 {@code SqlSegment} 的对象引用写进 SQL。</li>
 * </ol>
 */
@DisplayName("条件构造器 SQL")
class WrapperSqlTest {

    @Test
    @DisplayName("基本比较运算按别名前缀拼装并用 AND 连接")
    void compares() {
        String sql = new QueryWrapper<>(UserEntity.class)
                .eq("id", 1).ne("age", 2).gt("age", 3).ge("age", 4).lt("age", 5).le("age", 6)
                .getSqlSegment();

        assertThat(sql).contains("t.id=#{tid}")
                .contains("AND t.age<>#{tage}")
                .contains("AND t.age>#{tage2}")
                .contains("AND t.age>=#{tage3}")
                .contains("AND t.age<#{tage4}")
                .contains("AND t.age<=#{tage5}");

        // 显式指定别名时直接使用别名，不再重复加前缀。
        assertThat(new QueryWrapper<>(UserEntity.class).eq("u.id", 1).getSqlSegment()).contains("u.id=#{uid}");
        // 条件不成立时整段忽略。
        assertThat(new QueryWrapper<>(UserEntity.class).eq(false, "id", 1).getSqlSegment())
                .doesNotContain("WHERE");
    }

    @Test
    @DisplayName("like 系列生成四种匹配模式")
    void likes() {
        assertThat(new QueryWrapper<>(UserEntity.class).like("user_name", "bo").getSqlSegment())
                .contains("user_name LIKE #{user_name}");
        assertThat(new QueryWrapper<>(UserEntity.class).notLike("user_name", "bo").getSqlSegment())
                .contains("user_name NOT LIKE #{user_name}");
        assertThat(new QueryWrapper<>(UserEntity.class).likeLeft("user_name", "bo").getSqlSegment())
                .contains("LIKE CONCAT('%',#{user_name})");
        assertThat(new QueryWrapper<>(UserEntity.class).likeRight("user_name", "bo").getSqlSegment())
                .contains("LIKE CONCAT(#{user_name},'%')");
        assertThat(new QueryWrapper<>(UserEntity.class).notLikeRight("user_name", "bo").getSqlSegment())
                .contains("NOT LIKE CONCAT(#{user_name},'%')");
    }

    @Test
    @DisplayName("between 作为首个条件不会被丢弃，后续条件补 AND")
    void betweenIsNeverDropped() {
        assertThat(new QueryWrapper<>(UserEntity.class).between("age", 1, 9).getSqlSegment())
                .contains("WHERE")
                .contains("t.age BETWEEN #{tagelow} AND #{tagehigh}");

        assertThat(new QueryWrapper<>(UserEntity.class).notBetween("age", 1, 9).getSqlSegment())
                .contains("NOT BETWEEN");

        String combined = new QueryWrapper<>(UserEntity.class).eq("id", 1).between("age", 1, 9).getSqlSegment();
        assertThat(combined).contains("t.id=#{tid} AND (t.age BETWEEN");
    }

    @Test
    @DisplayName("in / notIn / inSql / notInSql 正常生成")
    void inClauses() {
        assertThat(new QueryWrapper<>(UserEntity.class).in("age", Arrays.asList(1, 2)).getSqlSegment())
                .contains("t.age IN(#{tage},#{tage2})");
        assertThat(new QueryWrapper<>(UserEntity.class).in("age", 1, 2).getSqlSegment())
                .contains("t.age IN(#{tage},#{tage2})");
        assertThat(new QueryWrapper<>(UserEntity.class).notIn("age", 1, 2).getSqlSegment())
                .contains("t.age NOT IN(#{tage},#{tage2})");

        String inSql = new QueryWrapper<>(UserEntity.class).inSql("id", "select id from t_user").getSqlSegment();
        assertThat(inSql).contains("WHERE").contains("t.id").contains("IN(select id from t_user)");

        String notInSql = new QueryWrapper<>(UserEntity.class)
                .eq("id", 1).notInSql("id", "select id from t_user").getSqlSegment();
        assertThat(notInSql).contains("NOT IN(select id from t_user)");
    }

    @Test
    @DisplayName("isNull / isNotNull 与 apply 都能作为首个条件")
    void nullAndApplyClauses() {
        assertThat(new QueryWrapper<>(UserEntity.class).isNull("age").getSqlSegment())
                .contains("WHERE").contains("t.age").contains("IS NULL");
        assertThat(new QueryWrapper<>(UserEntity.class).isNotNull("age").getSqlSegment())
                .contains("IS NOT NULL");

        String applied = new QueryWrapper<>(UserEntity.class).apply("age > {0}", 18).getSqlSegment();
        assertThat(applied).contains("WHERE").contains("age > #{SWAKVAL}");

        String chained = new QueryWrapper<>(UserEntity.class).eq("id", 1).apply("age > {0}", 18).getSqlSegment();
        assertThat(chained).contains("t.id=#{tid} AND age > #{SWAKVAL}");
    }

    @Test
    @DisplayName("嵌套条件只拼接子条件，不重复 SELECT/FROM")
    void nestedRendersConditionsOnly() {
        String nested = new QueryWrapper<>(UserEntity.class)
                .nested(w -> w.eq("a", 1).or().eq("b", 2))
                .getSqlSegment();

        assertThat(nested).contains("WHERE").contains("(t.a=#{ta} OR t.b=#{tb})");
        assertThat(nested.substring(nested.indexOf("WHERE"))).doesNotContain("SELECT").doesNotContain("FROM");

        String withOuter = new QueryWrapper<>(UserEntity.class)
                .eq("id", 1)
                .and(w -> w.eq("a", 1).or().eq("b", 2))
                .getSqlSegment();
        assertThat(withOuter).contains("t.id=#{tid} AND (t.a=#{ta} OR t.b=#{tb})");

        String notted = new QueryWrapper<>(UserEntity.class).eq("id", 1).not(w -> w.eq("a", 1)).getSqlSegment();
        assertThat(notted).contains("NOT").contains("(t.a=#{ta})");

        String or = new QueryWrapper<>(UserEntity.class).eq("id", 1).or(w -> w.eq("a", 1)).getSqlSegment();
        assertThat(or).contains("OR (t.a=#{ta})");
    }

    @Test
    @DisplayName("group by / having / order by 渲染真实列名")
    void groupAndOrder() {
        assertThat(new QueryWrapper<>(UserEntity.class).groupBy("age").getSqlSegment())
                .contains("GROUP BY t.age")
                .doesNotContain("StringSqlSegment");

        assertThat(new QueryWrapper<>(UserEntity.class).groupBy("age", "user_name").getSqlSegment())
                .contains("GROUP BY t.age,t.user_name");

        String having = new QueryWrapper<>(UserEntity.class)
                .groupBy("age").having("count(1) > {0}", 1).getSqlSegment();
        assertThat(having).contains("GROUP BY t.age").contains("HAVING count(1) > #{SWAKVAL}");

        assertThat(new QueryWrapper<>(UserEntity.class).orderBy(true, false, "age").getSqlSegment())
                .contains("ORDER BY t.age");
        assertThat(new QueryWrapper<>(UserEntity.class).orderBy(true, true, "age").getSqlSegment())
                .contains("ASC");
    }

    @Test
    @DisplayName("select / from / join / last 改变整条语句的结构")
    void selectFromJoinLast() {
        assertThat(new QueryWrapper<>(UserEntity.class).select(UserEntity::getId, UserEntity::getUserName)
                .getSqlSegment()).contains("SELECT t.id,t.user_name FROM t_user t");

        assertThat(new QueryWrapper<>(UserEntity.class).from("t_user", "u").eq("id", 1).getSqlSegment())
                .contains("FROM t_user u").contains("u.id=#{uid}");

        assertThat(new QueryWrapper<>(UserEntity.class).leftJoin("t_dept on t_user.dept_id = t_dept.id")
                .getSqlSegment()).contains("LEFT JOIN").contains("t_dept on t_user.dept_id = t_dept.id");

        assertThat(new QueryWrapper<>(UserEntity.class).eq("id", 1).last("limit 1").getSqlSegment())
                .endsWith("limit 1");
    }

    @Test
    @DisplayName("getBoundSql 生成 ? 占位，getStaticSql 内联字面值")
    void boundAndStaticSql() {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>(UserEntity.class)
                .eq("id", 1).like("user_name", "bo");

        assertThat(wrapper.getBoundSql().getSql()).contains("t.id=?").doesNotContain("#{");
        assertThat(wrapper.getStaticSql()).contains("t.id=1").contains("'bo'");
    }

    @Test
    @DisplayName("clear 清空全部条件")
    void clearResetsConditions() {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>(UserEntity.class).eq("id", 1).between("age", 1, 2);

        wrapper.clear();

        assertThat(wrapper.getSqlSegment()).doesNotContain("WHERE");
    }

    @Test
    @DisplayName("UpdateWrapper 的 set 立即渲染占位符，delete 复用同一套条件")
    void updateWrapper() {
        UpdateWrapper<UserEntity> update = new UpdateWrapper<>(UserEntity.class);
        update.set(true, "user_name", "bob").setSql(true, "age = 20").eq("id", 1);

        String sql = update.getSqlSegment();
        assertThat(sql).contains("SET user_name=#{SWAKVAL},age = 20")
                .contains("WHERE (t.id=#{tid})")
                .doesNotContain("StringSqlSegment");

        update.setSqlKeyword(SqlKeyword.DELETE);
        assertThat(update.getSqlSegment()).startsWith(" DELETE").contains("FROM t_user");
    }

    @Test
    @DisplayName("SaveWrapper 生成纯 insert，不追加 SELECT 尾巴")
    void saveWrapper() {
        SaveWrapper<UserEntity> save = new SaveWrapper<>(UserEntity.class);
        save.addColumn("user_name", "bob").addColumn("age", 20);

        assertThat(save.getSqlSet()).isEqualTo("(user_name,age) VALUES(#{user_name},#{age})");
        String sql = save.getSqlSegment();
        assertThat(sql).contains("INSERT").contains("INTO t_user").contains("VALUES(#{user_name},#{age})");
        assertThat(sql).doesNotContain("SELECT").doesNotContain("null");
    }

    @Test
    @DisplayName("SaveWrapper.setEntity 按字段策略跳过 null 字段")
    void saveWrapperSkipsNullFields() {
        UserEntity entity = new UserEntity();
        entity.setUserName("bob");
        // age 保持 null，默认策略下不应出现在 INSERT 列表里。

        SaveWrapper<UserEntity> save = new SaveWrapper<>(UserEntity.class);
        save.setEntity(entity);

        assertThat(save.getSqlSet()).contains("user_name").doesNotContain("age");
    }
}
