package com.swak.jdbc.test;

import com.alibaba.fastjson2.JSON;
import com.swak.jdbc.conditions.chain.QueryChainWrapper;
import com.swak.jdbc.conditions.chain.SwakChainWrappers;
import com.swak.jdbc.parser.SwakBoundSql;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * JoinWrapperTest.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public class JoinWrapperTest {


    @Test
    public void joinWrapper(){
         QueryChainWrapper<Object> query = SwakChainWrappers.query();
        query.select("a.*","e.*").from("flow_stage_log","a")
                .leftJoin("chamber c on c.id=a.chamber_id")
                .leftJoin("eqp e on e.id=c.eqp_id")
                .eq("c.id", 755)
                .last(" limit 1");
        SwakBoundSql boundSql = query.getWrapper().getBoundSql();
        System.out.println(boundSql.getSql());
        System.out.println(JSON.toJSONString(boundSql.getParamObjectValues()));
    }
}
