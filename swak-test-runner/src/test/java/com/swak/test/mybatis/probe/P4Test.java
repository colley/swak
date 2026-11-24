package com.swak.test.mybatis.probe;

import com.swak.jdbc.conditions.query.QueryWrapper;
import com.swak.jdbc.conditions.update.SaveWrapper;
import com.swak.jdbc.conditions.update.UpdateWrapper;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.test.mybatis.fixture.UserEntity;
import org.junit.jupiter.api.Test;

class P4Test {
    @Test
    void probe() {
        SaveWrapper<UserEntity> s = new SaveWrapper<>(UserEntity.class);
        s.addColumn("user_name", "bob").addColumn("age", 20);
        System.out.println("P4[save] " + s.getSqlSegment() + " | set=" + s.getSqlSet());

        UpdateWrapper<UserEntity> u = new UpdateWrapper<>(UserEntity.class);
        u.setSqlKeyword(SqlKeyword.DELETE);
        u.eq("id", 1);
        System.out.println("P4[delete] " + u.getSqlSegment());

        QueryWrapper<UserEntity> q = new QueryWrapper<>(UserEntity.class).eq("id", 1).like("user_name", "bo");
        System.out.println("P4[boundSql] " + q.getBoundSql().getSql());
        System.out.println("P4[staticSql] " + q.getStaticSql());
        q.clear();
        System.out.println("P4[afterClear] [" + q.getSqlSegment() + "]");
    }
}
