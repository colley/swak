package com.swak.easyjob;

import com.alibaba.fastjson2.JSON;
import com.swak.easyjob.annotation.EasyJobInfo;
import com.swak.easyjob.annotation.ScheduleType;
import com.swak.easyjob.mapper.EasyJobMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.CronExpression;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The type Job schedule handler.
 *
 * @author colley.ma
 * @since 2022 /10/27 10:25
 */
@Slf4j
public class JobScheduleHandler {
    /**
     * The constant PRE_READ_MS.
     */
    public static final long PRE_READ_MS = 5000;
    private final EasyJobConfig easyJobConfig;
    private final EasyJobMapper easyJobMapper;
    private final JobTriggerPoolHandler jobTriggerPoolHandler;
    private final EasyScheduledConfigurerFactory easyScheduledConfigurerFactory;
    private Thread scheduleThread;
    private Thread ringThread;
    private volatile boolean scheduleThreadToStop = false;
    private volatile boolean ringThreadToStop = false;
    private volatile Map<Integer, List<String>> ringData = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Job schedule handler.
     *
     * @param easyJobConfig                  the easy job config
     * @param easyJobMapper                  the easy job mapper
     * @param easyScheduledConfigurerFactory the easy scheduled configurer factory
     */
    public JobScheduleHandler(EasyJobConfig easyJobConfig, EasyJobMapper easyJobMapper, EasyScheduledConfigurerFactory easyScheduledConfigurerFactory) {
        this.easyJobConfig = easyJobConfig;
        this.jobTriggerPoolHandler = new JobTriggerPoolHandler(this, easyJobMapper);
        this.easyScheduledConfigurerFactory = easyScheduledConfigurerFactory;
        this.easyJobMapper = easyJobMapper;
    }

    /**
     * Generate next valid time date.
     *
     * @param jobInfo  the job info
     * @param fromTime the  time
     * @return the date
     * @throws Exception the exception
     */
    public static Date generateNextValidTime(EasyJobInfo jobInfo, Date fromTime) throws Exception {
        if (ScheduleType.CRON.match(jobInfo.getScheduleType())) {
            return new CronExpression(jobInfo.getScheduleConf()).getNextValidTimeAfter(fromTime);
        } else if (ScheduleType.FIX_RATE.match(jobInfo.getScheduleType())) {
            return new Date(fromTime.getTime() + Integer.parseInt(jobInfo.getScheduleConf()) * 1000L);
        }
        return null;
    }

    /**
     * 测试
     *
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException {
        CronExpression cronExpression = new CronExpression("0 */1 * * * ?");
        Date nextDate = cronExpression.getNextValidTimeAfter(new Date());
        for (int i = 0; i < 10; i++) {
            nextDate = cronExpression.getNextValidTimeAfter(nextDate);
            System.out.println(LocalDateTime.ofInstant(nextDate.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    /**
     * 启动
     */
    public void start() {
        scheduleStart();
        ringStart();
        jobTriggerPoolHandler.start();
    }

    private void scheduleStart() {
        scheduleThread = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(5000 - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                if (!scheduleThreadToStop) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info(">>>>>>>>> init easy job scheduler success.");
            while (!scheduleThreadToStop) {
                // Scan Job
                long start = System.currentTimeMillis();
                Connection conn = null;
                Boolean connAutoCommit = null;
                PreparedStatement preparedStatement = null;
                boolean preReadSuc = true;
                try {
                    conn = easyJobMapper.getJdbcTemplate().getDataSource().getConnection();
                    connAutoCommit = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                    preparedStatement = conn.prepareStatement("select * from easy_job_lock where lock_name = 'schedule_lock' and app_name='" + easyJobConfig.getAppName() + "' for update");
                    preparedStatement.execute();
                    // tx start
                    // 1、pre read
                    long nowTime = System.currentTimeMillis();
                    List<EasyJobInfo> scheduleList = easyJobMapper.scheduleJobQuery(nowTime + PRE_READ_MS);
                    if (CollectionUtils.isNotEmpty(scheduleList)) {
                        if (log.isDebugEnabled()) {
                            log.debug("EasyJobInfo list :{}", JSON.toJSONString(scheduleList));
                        }
                        for (EasyJobInfo jobInfo : scheduleList) {
                            // time-ring jump
                            //105  101  5
                            log.debug(">>>>>>>>>  easy job infos,jobName = {}", jobInfo.getJobName());
                            if (nowTime > jobInfo.getTriggerNextTime() + PRE_READ_MS) {
                                // 2.1、trigger-expire > 5s：pass && make next-trigger-time
                                log.warn(">>>>>>>>>>> easy-job, schedule misfire, jobName = " + jobInfo.getJobName());
                                jobTriggerPoolHandler.trigger(jobInfo.getJobName());
                                // 2、fresh next
                                refreshNextValidTime(jobInfo, new Date());
                            } else if (nowTime > jobInfo.getTriggerNextTime()) {
                                // 2.2、trigger-expire < 5s：direct-trigger && make next-trigger-time
                                // 1、trigger
                                jobTriggerPoolHandler.trigger(jobInfo.getJobName());
                                log.debug(">>>>>>>>>>> easy-job, schedule push trigger : jobId = " + jobInfo.getId());
                                // 2、fresh next
                                refreshNextValidTime(jobInfo, new Date());
                            } else {
                                // 2.3、trigger-pre-read：time-ring trigger && make next-trigger-time
                                // 1、make ring second
                                int ringSecond = (int) ((jobInfo.getTriggerNextTime() / 1000) % 60);
                                // 2、push time ring
                                pushTimeRing(ringSecond, jobInfo.getJobName());
                                // 3、fresh next
                                refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));
                            }
                        }
                        // 3、update trigger info
                        for (EasyJobInfo jobInfo : scheduleList) {
                            easyJobMapper.scheduleUpdate(jobInfo);
                        }
                    } else {
                        preReadSuc = false;
                    }
                    // tx stop
                } catch (Exception e) {
                    if (!scheduleThreadToStop) {
                        log.error(">>>>>>>>>>> easy-job, JobScheduleHandler#scheduleThread error", e);
                    }
                } finally {

                    // commit
                    if (conn != null) {
                        try {
                            conn.commit();
                        } catch (SQLException e) {
                            if (!scheduleThreadToStop) {
                                log.error(e.getMessage(), e);
                            }
                        }
                        try {
                            conn.setAutoCommit(connAutoCommit);
                        } catch (SQLException e) {
                            if (!scheduleThreadToStop) {
                                log.error(e.getMessage(), e);
                            }
                        }
                        try {
                            conn.close();
                        } catch (SQLException e) {
                            if (!scheduleThreadToStop) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }

                    // close PreparedStatement
                    if (null != preparedStatement) {
                        try {
                            preparedStatement.close();
                        } catch (SQLException e) {
                            if (!scheduleThreadToStop) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                }
                long cost = System.currentTimeMillis() - start;
                // Wait seconds, align second
                if (cost < 1000) {  // scan-overtime, not wait
                    try {
                        // pre-read period: success > scan each second; fail > skip this period;
                        TimeUnit.MILLISECONDS.sleep((preReadSuc ? 1000 : PRE_READ_MS) - System.currentTimeMillis() % 1000);
                    } catch (InterruptedException e) {
                        if (!scheduleThreadToStop) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
            log.debug(">>>>>>>>>>> easy-job, JobScheduleHandler#scheduleThread stop");

        });

        scheduleThread.setDaemon(true);
        scheduleThread.setName("EasyJob-scheduleThread");
        scheduleThread.start();

    }

    private void ringStart() {
        // ring thread
        ringThread = new Thread(() -> {
            while (!ringThreadToStop) {
                // align second
                try {
                    TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
                } catch (InterruptedException e) {
                    if (!ringThreadToStop) {
                        log.error(e.getMessage(), e);
                    }
                }
                try {
                    // second data
                    List<String> ringItemData = new ArrayList<>();
                    int nowSecond = Calendar.getInstance().get(Calendar.SECOND);   // 避免处理耗时太长，跨过刻度，向前校验一个刻度；
                    for (int i = 0; i < 2; i++) {
                        List<String> tmpData = ringData.remove((nowSecond + 60 - i) % 60);
                        if (tmpData != null) {
                            ringItemData.addAll(tmpData);
                        }
                    }
                    // ring trigger
                    log.debug(">>>>>>>>>>> easy-job, time-ring beat : " + nowSecond + " = " + Collections.singletonList(ringItemData));
                    if (ringItemData.size() > 0) {
                        // do trigger
                        for (String jobName : ringItemData) {
                            // do trigger
                            jobTriggerPoolHandler.trigger(jobName);
                        }
                        // clear
                        ringItemData.clear();
                    }
                } catch (Exception e) {
                    if (!ringThreadToStop) {
                        log.error(">>>>>>>>>>> easy-job, JobScheduleHandler#ringThread error", e);
                    }
                }
            }
            log.debug(">>>>>>>>>>> easy-job, JobScheduleHandler#ringThread stop");
        });
        ringThread.setDaemon(true);
        ringThread.setName("EasyJob-RingThread");
        ringThread.start();
    }

    private void pushTimeRing(int ringSecond, String jobName) {
        // push async ring
        List<String> ringItemData = ringData.computeIfAbsent(ringSecond, k -> new ArrayList<>());
        ringItemData.add(jobName);
        log.debug(">>>>>>>>>>> easy-job, schedule push time-ring : " + ringSecond + " = " + Arrays.asList(ringItemData));
    }

    /**
     * Stop.
     */
    public void stop() {
        // 1、stop schedule
        this.scheduleThreadToStop = true;
        try {
            /** wait **/
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        if (scheduleThread.getState() != Thread.State.TERMINATED) {
            // interrupt and wait
            scheduleThread.interrupt();
            try {
                scheduleThread.join();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        // if has ring data
        boolean hasRingData = false;
        if (!ringData.isEmpty()) {
            for (int second : ringData.keySet()) {
                List<String> tmpData = ringData.get(second);
                if (tmpData != null && tmpData.size() > 0) {
                    hasRingData = true;
                    break;
                }
            }
        }
        if (hasRingData) {
            try {
                TimeUnit.SECONDS.sleep(8);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        // stop ring (wait job-in-memory stop)
        ringThreadToStop = true;
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        if (ringThread.getState() != Thread.State.TERMINATED) {
            // interrupt and wait
            ringThread.interrupt();
            try {
                ringThread.join();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        jobTriggerPoolHandler.stop();
        log.debug(">>>>>>>>>>> easy-job, JobScheduleHandler stop");
    }

    private void refreshNextValidTime(EasyJobInfo jobInfo, Date fromTime) throws Exception {
        Date nextValidTime = generateNextValidTime(jobInfo, fromTime);
        if (nextValidTime != null) {
            jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
            jobInfo.setTriggerNextTime(nextValidTime.getTime());
        } else {
            jobInfo.setTriggerLastTime(0);
            jobInfo.setTriggerNextTime(0);
            log.warn(">>>>>>>>>>> easy-job, refreshNextValidTime fail for job: jobId={}, scheduleType={}, scheduleConf={}",
                    jobInfo.getId(), jobInfo.getScheduleType(), jobInfo.getScheduleConf());
        }
    }

    /**
     * Resolve expression as boolean.
     *
     * @param value the value
     * @return the boolean
     */
    public Boolean resolveAsBoolean(String value) {
        return easyScheduledConfigurerFactory.resolveAsBoolean(value);
    }

    /**
     * Resolve expression object.
     *
     * @param value the value
     * @return the object
     */
    public Object resolveExpression(String value) {
        return easyScheduledConfigurerFactory.resolveExpression(value);
    }
}

