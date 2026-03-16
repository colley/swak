package com.swak.jdbc.test;

import com.alibaba.fastjson2.JSON;
import com.swak.jdbc.conditions.chain.QueryChainWrapper;
import com.swak.jdbc.conditions.chain.SwakChainWrappers;
import com.swak.jdbc.parser.SwakBoundSql;
import com.swak.jdbc.segments.JunctionSqlSegment;
import com.swak.jdbc.toolkit.JdbcRestrictions;
import org.junit.Test;

/**
 * applyWhereTest.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public class ApplyWhereTest {

    /**
     * 测试 apply
     * @since 2023/3/6
     * SELECT * FROM flow_stage_log t WHERE (t.a1=? AND a1>2 AND (a0>2 OR a1>2))
     * [1]
     */
    @Test
    public void applyWrapper() {
        boolean isOr = true;
        QueryChainWrapper<Object> query = SwakChainWrappers.query();
        query.select("*").from("flow_stage_log");
//        for (int i = 4; i < 7; i++) {
//            query.where(JdbcRestrictions.apply(String.format("a%s=?", i)));
//        }
        if (isOr) {
            JunctionSqlSegment or = JdbcRestrictions.or();
            for (int i = 0; i < 2; i++) {
                String sql = String.format("a%s>2", i);
                or.addSqlSegment(JdbcRestrictions.apply(sql));
            }
            query.where(or);
        }
        SwakBoundSql boundSql = query.getWrapper().getBoundSql();
        System.out.println(boundSql.getSql());
        System.out.println(JSON.toJSONString(boundSql.getParamObjectValues()));
    }
}
