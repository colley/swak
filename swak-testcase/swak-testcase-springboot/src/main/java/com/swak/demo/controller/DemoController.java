package com.swak.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swak.common.dto.*;
import com.swak.common.util.DateTimeUtils;
import com.swak.core.sync.DistributedLock;
import com.swak.demo.entity.DetectionParameter;
import com.swak.demo.mapper.DetectionParameterMapper;
import com.swak.jdbc.conditions.query.LambdaQueryWrapper;
import com.swak.jdbc.spi.SwakJdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 17:03
 */
@Validated
@RestController
public class DemoController {

    @Resource
    private DetectionParameterMapper detectionParameterMapper;

    @Resource
    private SwakJdbcTemplate swakJdbcTemplate;

    @Resource
    private DistributedLock distributedLock;

    @GetMapping("/infos")
    public Response<String> infos(){
        String lockKey = "infos.lock";
        try {
            if(distributedLock.acquireLock(lockKey,10L, TimeUnit.SECONDS)){
                try {
                    TimeUnit.SECONDS.sleep(5L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Response.success("OK");
            }
            return Response.success("lock");
        }finally {
            distributedLock.releaseLock(lockKey);
        }
    }


    @GetMapping("/detection/list")
    public Response<Pagination<DetectionParameter>> detectionList(){
        long start = System.currentTimeMillis();
        QueryWrapper<DetectionParameter> queryWrapper = new QueryWrapper<>();
        Page<DetectionParameter> page = detectionParameterMapper.selectPage(new Page<>(1, 10), queryWrapper);

        String result =" Consume Time：" + (System.currentTimeMillis() -start) + " ms "  +
                 DateTimeUtils.date2String(new Date());

        System.err.println(result);
        return Response.success(new Pagination<>(page.getRecords(),
                PageInfo.newPageInfo(BigDecimal.valueOf(page.getCurrent()).intValue(),BigDecimal.valueOf(page.getSize()).intValue()
                        ,page.getTotal())));
    }

    @GetMapping("/detection/list/v2")
    public Response<Pagination<DetectionParameter>> detectionListV2(){
        long start = System.currentTimeMillis();
        LambdaQueryWrapper<DetectionParameter> queryWrapper = new LambdaQueryWrapper<>(DetectionParameter.class)
                .selectAll(DetectionParameter.class);
        SwakPage<DetectionParameter> parameterSwakPage = swakJdbcTemplate.selectPage(SwakPage.of(1, 10), queryWrapper);
        String result =" Consume Time：" + (System.currentTimeMillis() -start) + " ms "  +
                DateTimeUtils.date2String(new Date());

        System.err.println(result);
        return Response.success(new Pagination<>(parameterSwakPage.getRecords(),
                PageInfo.newPageInfo(BigDecimal.valueOf(parameterSwakPage.getCurrent()).intValue(),
                        BigDecimal.valueOf(parameterSwakPage.getSize()).intValue(),parameterSwakPage.getTotal())));
    }

}
