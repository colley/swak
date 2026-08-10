package com.swak.jdbc.test;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.swak.jdbc.conditions.chain.LambdaChainWrapper;
import com.swak.jdbc.conditions.chain.LambdaSaveChainWrapper;
import com.swak.jdbc.conditions.chain.SwakChainWrappers;
import com.swak.jdbc.conditions.query.LambdaQueryWrapper;
import com.swak.jdbc.test.entity.FlowStageLog;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * QueryListTest.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public class QueryWrapperTest {

    @Test
    public void queryList(){
//        LambdaQueryWrapper<FlowStageLog> queryJoinWrapper = new LambdaQueryWrapper<>(new FlowStageLog());
//        queryJoinWrapper.select(FlowStageLog::getChamberId,FlowStageLog::getWorkflowId)
//                                .eq(FlowStageLog::getChamberId,1823)
//                                        .last(" limit 10");
//		System.out.println(queryJoinWrapper.getBoundSql());

		LambdaChainWrapper<FlowStageLog> chainWrapper = SwakChainWrappers.lambdaQuery(FlowStageLog.class);
		chainWrapper.in(FlowStageLog::getChamberId, Lists.newArrayList(1823,1824));
		chainWrapper.eq(FlowStageLog::getLogicId, 123);
		System.out.println(chainWrapper.getWrapper().getBoundSql().getSql());
		System.out.println(JSON.toJSONString(chainWrapper.getWrapper().getBoundSql().getParamObjectValues()));


//        List<FlowStageLog> mapList = SwakChainWrappers.lambdaQuery(FlowStageLog.class)
//                .eq(FlowStageLog::getChamberId,1823)
//                .orderByAsc(FlowStageLog::getLogicId)
//                .last(" limit 10").list();
//        System.out.println(JSON.toJSONString(mapList));
//
//        List<Map<String,Object>> listMap = SwakChainWrappers.query().select(FlowStageLog::getChamberId, FlowStageLog::getWorkflowId)
//                .from("flow_stage_log")
//                .eq(FlowStageLog::getChamberId, 1823)
//                .last(" limit 10").listMap();
//
//        System.out.println(JSON.toJSONString(listMap));
    }
}
