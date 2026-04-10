package com.swak.jdbc.test;

import com.alibaba.fastjson2.JSON;
import com.swak.common.util.UUIDHexGenerator;
import com.swak.jdbc.conditions.chain.SwakChainWrappers;
import com.swak.jdbc.conditions.update.LambdaSaveWrapper;
import com.swak.jdbc.conditions.update.LambdaUpdateWrapper;
import com.swak.jdbc.parser.SwakBoundSql;
import com.swak.jdbc.test.entity.FlowStageLog;
import org.junit.Test;

/**
 * InsertWrapperTest.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public class InsertOrUpdateWrapperTest {


    @Test
    public void updateWrapper(){
        FlowStageLog flowStageLog = new FlowStageLog();
        LambdaUpdateWrapper<FlowStageLog> updateWrapper = new LambdaUpdateWrapper<>(flowStageLog);
        updateWrapper.set(FlowStageLog::getChamberId,1823)
                .set(FlowStageLog::getWorkflowId, UUIDHexGenerator.generator())
                .eq(FlowStageLog::getChamberId,1823);
        SwakBoundSql boundSql = updateWrapper.getBoundSql();
        System.out.println(boundSql.getSql());
        System.out.println(JSON.toJSONString(boundSql.getParamObjectValues()));
    }

    @Test
    public void insertWrapper(){
        FlowStageLog flowStageLog = new FlowStageLog().setChamberId(1823L).setWorkflowId(UUIDHexGenerator.generator());
        LambdaSaveWrapper<FlowStageLog> insertWrapper = new LambdaSaveWrapper<>(flowStageLog);
        SwakBoundSql boundSql = insertWrapper.getBoundSql();
        System.out.println(boundSql.getSql());
        System.out.println(JSON.toJSONString(boundSql.getParamObjectValues()));
       // boolean saved = SwakChainWrappers.lambdaSave().save(flowStageLog);
        //System.out.println(saved);
        //int rows = swakJdbcTemplate.save(insertWrapper);
        // System.out.println(JSON.toJSONString(rows));
    }
}
