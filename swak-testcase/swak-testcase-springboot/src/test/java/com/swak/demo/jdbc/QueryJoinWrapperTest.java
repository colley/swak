package com.swak.demo.jdbc;

import com.alibaba.fastjson2.JSON;
import com.swak.common.util.UUIDHexGenerator;
import com.swak.demo.SwakCaseApplication;
import com.swak.demo.entity.FlowStageLog;
import com.swak.jdbc.conditions.chain.QueryChainWrapper;
import com.swak.jdbc.conditions.chain.SwakChainWrappers;
import com.swak.jdbc.conditions.update.LambdaSaveWrapper;
import com.swak.jdbc.conditions.update.LambdaUpdateWrapper;
import com.swak.jdbc.parser.SwakBoundSql;
import com.swak.jdbc.spi.SwakJdbcTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * LambdaJoinWrapperTest.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SwakCaseApplication.class,webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class QueryJoinWrapperTest {

    @Resource
    private SwakJdbcTemplate swakJdbcTemplate;

    @Test
    public void queryList(){
//        LambdaQueryWrapper<FlowStageLog> queryJoinWrapper = new LambdaQueryWrapper<>(new FlowStageLog());
//        queryJoinWrapper.select(FlowStageLog::getChamberId,FlowStageLog::getWorkflowId)
//                                .eq(FlowStageLog::getChamberId,1823)
//                                        .last(" limit 10");

        List<FlowStageLog> mapList = SwakChainWrappers.lambdaQuery(FlowStageLog.class)
                .eq(FlowStageLog::getChamberId,1823)
                .orderByAsc(FlowStageLog::getLogicId)
                .last(" limit 10").list();
        System.out.println(JSON.toJSONString(mapList));

        List<Map<String,Object>> listMap = SwakChainWrappers.query().select(FlowStageLog::getChamberId, FlowStageLog::getWorkflowId)
                .from("flow_stage_log")
                .eq(FlowStageLog::getChamberId, 1823)
                .last(" limit 10").listMap();

        System.out.println(JSON.toJSONString(listMap));
    }


    @Test
    public void updateWrapper(){
        FlowStageLog flowStageLog = new FlowStageLog();
        LambdaUpdateWrapper<FlowStageLog> updateWrapper = new LambdaUpdateWrapper<>(flowStageLog);
        updateWrapper.set(FlowStageLog::getChamberId,1823)
                .set(FlowStageLog::getWorkflowId, UUIDHexGenerator.generator())
                .eq(FlowStageLog::getChamberId,1823);
        System.out.println(updateWrapper.getSqlSegment());
       int rows = swakJdbcTemplate.update(updateWrapper);
        System.out.println(JSON.toJSONString(rows));
    }


    @Test
    public void insertWrapper(){
        FlowStageLog flowStageLog = new FlowStageLog().setChamberId(1823L).setWorkflowId(UUIDHexGenerator.generator());
//        LambdaSaveWrapper<FlowStageLog> insertWrapper = new LambdaSaveWrapper<>();
//        insertWrapper.addEntity(flowStageLog);
//        SwakBoundSql boundSql = insertWrapper.getBoundSql();
//        System.out.println(boundSql.getSql());
//        System.out.println(JSON.toJSONString(boundSql.getParamObjectValues()));

        boolean saved = SwakChainWrappers.lambdaSave().save(flowStageLog);
        System.out.println(saved);
        //int rows = swakJdbcTemplate.save(insertWrapper);
       // System.out.println(JSON.toJSONString(rows));
    }


    @Test
    public void joinWrapper(){
        List<Map<String, Object>> list = SwakChainWrappers.query()
                .select("a.*","e.*").from("flow_stage_log","a")
                .leftJoin("chamber c on c.id=a.chamber_id")
                .leftJoin("eqp e on e.id=c.eqp_id")
                .eq("c.id", 755)
                .last(" limit 1")
                .listMap();

        System.out.println(JSON.toJSONString(list));
    }
}
