package com.swak.easyjob;


import com.alibaba.fastjson2.JSON;
import com.swak.common.dto.Response;
import com.swak.common.util.GetterUtil;
import com.swak.core.support.SpringBeanFactory;
import com.swak.easyjob.annotation.EasyJobInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Easy job trigger.
 *
 * @author yuanchao.ma
 * @since 2022 /8/24 15:49
 */
@Slf4j
public class EasyJobTrigger {

    private final JobScheduleHandler jobScheduleHandler;

    public EasyJobTrigger(JobScheduleHandler jobScheduleHandler) {
        this.jobScheduleHandler = jobScheduleHandler;
    }

    /**
     * Process trigger.
     *
     * @param jobInfo the job info
     */
    public void processTrigger(EasyJobInfo jobInfo) {
        EasyJobHandler easyJobHandler = SpringBeanFactory.getBean(jobInfo.getExecutorHandler(), EasyJobHandler.class);
        if (easyJobHandler != null) {
            EasyJobContext context = EasyJobContext.builder()
                    .executorHandler(easyJobHandler)
                    .jobName(jobInfo.getJobName())
                    .params(GetterUtil.getSplitStr(jobInfo.getExecutorParam()))
                    .shardingCount(jobInfo.getShardingCount())
                    .shardingNum(jobInfo.getShardingNum())
                    .scheduleConf(jobInfo.getScheduleConf())
                    .scheduleType(jobInfo.getScheduleType()).build();
            Response<Void> response = easyJobHandler.execute(context);
            if (!response.isSuccess()) {
                log.warn("[Swak-Job] execute error,jobName:{},context:{}", jobInfo.getJobName(), JSON.toJSONString(context));
            }
        }
    }
}
