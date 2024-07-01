package com.swak.easyjob.annotation;


import com.alibaba.fastjson2.JSON;
import com.swak.common.dto.Response;
import com.swak.common.util.GetterUtil;
import com.swak.core.support.SpringBeanFactory;
import com.swak.easyjob.EasyJobContext;
import com.swak.easyjob.EasyJobHandler;
import com.swak.easyjob.JobScheduleHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;


@Slf4j
public class EasyJobTask implements Runnable {
    private final EasyJobInfo easyJobInfo;

    private final JobScheduleHandler jobScheduleHandler;

    /**
     * Instantiates a new Easy job task.
     *
     * @param easyJobInfo the easy job info
     */
    public EasyJobTask(EasyJobInfo easyJobInfo, JobScheduleHandler jobScheduleHandler) {
        this.easyJobInfo = easyJobInfo;
        this.jobScheduleHandler = jobScheduleHandler;
    }

    @Override
    public void run() {
        Boolean enabled = jobScheduleHandler.resolveAsBoolean(easyJobInfo.getScheduleEnabled());
        if (!Optional.ofNullable(enabled).orElse(true)) {
            log.debug("[easy-job]#任务调度未开启，jobName:{}", easyJobInfo.getJobName());
            return;
        }
        //非分布式
        EasyJobHandler easyJobHandler = SpringBeanFactory.getBean(easyJobInfo.getExecutorHandler(), EasyJobHandler.class);
        if (easyJobHandler != null) {
            EasyJobContext context = EasyJobContext.builder()
                    .executorHandler(easyJobHandler)
                    .jobName(easyJobInfo.getJobName())
                    .params(GetterUtil.getSplitStr(easyJobInfo.getExecutorParam()))
                    .shardingCount(easyJobInfo.getShardingCount())
                    .shardingNum(easyJobInfo.getShardingNum())
                    .scheduleConf(easyJobInfo.getScheduleConf())
                    .scheduleType(easyJobInfo.getScheduleType()).build();
            Response<Void> response = easyJobHandler.execute(context);
            if (!response.isSuccess()) {
                log.warn("easy-job execute error,jobName:{},context:{}", easyJobInfo.getJobName(), JSON.toJSONString(context));
            }
        }
    }
}
