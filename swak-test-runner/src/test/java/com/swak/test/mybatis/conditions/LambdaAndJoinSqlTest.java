package com.swak.test.mybatis.conditions;

import com.swak.jdbc.conditions.query.LambdaQueryWrapper;
import com.swak.jdbc.conditions.query.QueryWrapper;
import com.swak.jdbc.conditions.update.LambdaSaveWrapper;
import com.swak.jdbc.conditions.update.LambdaUpdateWrapper;
import com.swak.jdbc.conditions.update.SaveWrapper;
import com.swak.jdbc.conditions.update.UpdateWrapper;
import com.swak.test.mybatis.fixture.DeptEntity;
import com.swak.test.mybatis.fixture.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lambda 风格条件与 join 的 SQL 回归。
 */
@DisplayName("Lambda 条件与 Join")
class LambdaAndJoinSqlTest {

    @Test
    @DisplayName("Lambda 条件按字段映射成列名并补 AND")
    void lambdaConditions() {
        LambdaQueryWrapper<UserEntity> wrapper = new QueryWrapper<>(UserEntity.class).lambda();
        wrapper.eq(UserEntity::getAge, 1).in(UserEntity::getAge, 1, 2);

        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("t.age=#{tage} AND t.age IN(");
    }

    @Test
    @DisplayName("显式别名重载使用别名做前缀")
    void lambdaWithAlias() {
        LambdaQueryWrapper<UserEntity> wrapper = new QueryWrapper<>(UserEntity.class).lambda();
        wrapper.eq(true, "u", UserEntity::getAge, 1);

        assertThat(wrapper.getSqlSegment()).contains("u.age=#{uage}");
    }

    @Test
    @DisplayName("Lambda 分组与排序按列名渲染")
    void lambdaGroupAndOrder() {
        LambdaQueryWrapper<UserEntity> wrapper = new QueryWrapper<>(UserEntity.class).lambda();
        wrapper.groupBy(UserEntity::getAge).orderBy(true, false, UserEntity::getAge);

        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("GROUP BY t.age").contains("ORDER BY t.age");
        assertThat(sql).doesNotContain("StringSqlSegment");
    }

    @Test
    @DisplayName("LambdaUpdateWrapper 的 set 渲染占位符")
    void lambdaUpdate() {
        LambdaUpdateWrapper<UserEntity> wrapper = new UpdateWrapper<>(UserEntity.class).lambda();
        wrapper.set(UserEntity::getAge, 20).setSql("user_name = 'x'").eq(UserEntity::getId, 1);

        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("SET age=#{SWAKVAL},user_name = 'x'")
                .contains("WHERE (t.id=#{tid})")
                .doesNotContain("null");
    }

    @Test
    @DisplayName("LambdaSaveWrapper 生成纯 insert")
    void lambdaSave() {
        LambdaSaveWrapper<UserEntity> wrapper = new SaveWrapper<>(UserEntity.class).lambda();
        wrapper.addColumn(UserEntity::getUserName, "bob").addColumn(UserEntity::getAge, 20);

        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("INSERT").contains("INTO t_user").contains("VALUES(#{user_name},#{age})");
        assertThat(sql).doesNotContain("SELECT").doesNotContain("FROM null");
    }

    @Test
    @DisplayName("leftJoin 生成 ON 条件，不把 WHERE 关键字混进 ON")
    void leftJoin() {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>(UserEntity.class);
        wrapper.leftJoin(DeptEntity.class, DeptEntity::getId, UserEntity::getId);

        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("LEFT JOIN").contains("t_dept").contains("ON (");
        assertThat(sql).doesNotContain("ON  WHERE");
        assertThat(sql.indexOf("WHERE")).isEqualTo(-1);
    }

    @Test
    @DisplayName("join 与 where 条件可以共存，别名可指定")
    void joinWithConditionAndAlias() {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>(UserEntity.class);
        wrapper.leftJoin(DeptEntity.class, "d", DeptEntity::getId, UserEntity::getId)
                .eq(UserEntity::getAge, 18);

        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("LEFT JOIN  t_dept d ON (d.id = t.id)");
        assertThat(sql).contains("WHERE (t.age=#{tage})");
    }

    @Test
    @DisplayName("on 回调式 join 与直连式 join 结果一致")
    void joinWithOnConsumer() {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>(UserEntity.class);
        wrapper.leftJoin(DeptEntity.class, on -> on.eq(DeptEntity::getId, UserEntity::getId));

        assertThat(wrapper.getSqlSegment()).contains("ON (").doesNotContain("ON  WHERE");
    }
}
