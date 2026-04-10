package com.swak.easyjob;


import com.swak.easyjob.annotation.EasyJobInfo;
import com.swak.easyjob.mapper.EasyJobMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The type Job trigger pool handler.
 *
 * @author  yuanchao.ma
 * @since  2022/8/24 15:46
 */
@Slf4j
public class JobTriggerPoolHandler {
    private final EasyJobTrigger easyJobTrigger;
    private final JobScheduleHandler jobScheduleHandler;
    private final EasyJobMapper easyJobMapper;
    private ThreadPoolExecutor triggerPool = null;

    /**
     * Instantiates a new Job trigger pool handler.
     *
     * @param jobScheduleHandler the job schedule handler
     */
    public JobTriggerPoolHandler(JobScheduleHandler jobScheduleHandler, EasyJobMapper easyJobMapper) {
        this.jobScheduleHandler = jobScheduleHandler;
        this.easyJobMapper = easyJobMapper;
        this.easyJobTrigger = new EasyJobTrigger(jobScheduleHandler);
    }

    /**
     * Start.
     */
    public void start() {
        triggerPool = new ThreadPoolExecutor(
                5,
                15,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                r -> new Thread(r, "SwakJob-" + r.hashCode()));
    }


    /**
     * Stop.
     */
    public void stop() {
        triggerPool.shutdownNow();
        log.debug("[Swak-Job] >>>>>>>>> trigger thread pool shutdown success.");
    }

    /**
     * Add trigger.
     *
     * @param jobInfo the job info
     */
    public void addTrigger(EasyJobInfo jobInfo) {
        triggerPool.submit(() -> {
            try {
                // do trigger
                easyJobTrigger.processTrigger(jobInfo);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * Trigger.
     *
     * @param jobName the job name
     */
    public void trigger(String jobName) {
        EasyJobInfo jobInfo = easyJobMapper.findByJobName(jobName);
        if (!Objects.isNull(jobInfo)) {
            //设置开关，默认开启
            Boolean enabled = jobScheduleHandler.resolveAsBoolean(jobInfo.getScheduleEnabled());
            if (!Optional.ofNullable(enabled).orElse(true)) {
                log.debug("[Swak-Job] 任务调度未开启，jobName:{}", jobInfo.getJobName());
                return;
            }
            this.addTrigger(jobInfo);
        } else {
            log.warn("[Swak-Job] Job not found,jobName:{}", jobName);
        }
    }
}
