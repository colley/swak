package com.swak.easyjob;

import com.google.common.collect.Lists;
import com.swak.common.util.GetterUtil;
import com.swak.core.environment.SystemEnvironmentConfigurable;
import com.swak.easyjob.annotation.EasyJobInfo;
import com.swak.easyjob.annotation.EasyJobTask;
import com.swak.easyjob.annotation.ScheduleType;
import com.swak.easyjob.annotation.SwakScheduled;
import com.swak.easyjob.mapper.EasyJobMapper;
import com.swak.easyjob.mapper.EasyJobMapperImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Easy scheduled configurer factory.
 *
 * @author  yuanchao.ma
 * @since 2022 /8/29 16:37
 */
@Slf4j
public class EasyScheduledConfigurerFactory implements EasyScheduledConfigurer, EnvironmentAware {

    private final ListenerJobScope listenerScope = new ListenerJobScope();
    private EasyJobConfig easyJobConfig;
    private JobScheduleHandler jobScheduleHandler;
    private EasyJobMapper easyJobMapper;
    private BeanFactory beanFactory;
    private BeanExpressionResolver resolver;
    private BeanExpressionContext expressionContext;
    private ApplicationContext applicationContext;

    private SystemEnvironmentConfigurable systemConfig;


    /**
     * Instantiates a new Easy scheduled configurer factory.
     *
     * @param easyJobConfig the easy job config
     */
    public EasyScheduledConfigurerFactory(EasyJobConfig easyJobConfig,SystemEnvironmentConfigurable systemConfig) {
        this.easyJobConfig = easyJobConfig;
        Assert.notNull(easyJobConfig, "easyJobConfig is required!");
        Assert.hasLength(easyJobConfig.getAppName(), "appName is required!");
        Assert.notNull(easyJobConfig.getJdbcTemplate(), "JdbcTemplate is required!");
        Assert.hasLength(easyJobConfig.getJobInfoTableName(), "jobInfoTableName is required");
        Assert.hasLength(easyJobConfig.getJobLockTableName(), "JobLockTableName is required");
        this.easyJobMapper = new EasyJobMapperImpl(easyJobConfig);
        this.jobScheduleHandler = new JobScheduleHandler(this.easyJobConfig, easyJobMapper, this);
        this.systemConfig = systemConfig;
    }

    @Override
    public List<EasyJobInfo> doRegistration() {
        //注册，获取注解的定时任务
        List<EasyJobInfo> allEasyJobInfos = Lists.newArrayList();
        Map<String, Object> swakJobBeanMap = this.applicationContext.getBeansWithAnnotation(SwakScheduled.class);
        swakJobBeanMap.forEach((beanName, swakJobBean) -> {
            try {
                List<EasyJobInfo> easyJobInfos = getAllEasyJobInfos(beanName, (EasyJobHandler) swakJobBean);
                allEasyJobInfos.addAll(easyJobInfos);
            } catch (ParseException e) {
                throw new RuntimeException("job表达解析异常", e);
            }
        });
        return allEasyJobInfos;
    }

    private List<EasyJobInfo> getAllEasyJobInfos(String beanName, EasyJobHandler easyJobHandler) throws ParseException {
        List<EasyJobInfo> taskList = Lists.newArrayList();
        Class<?> extensionClz = easyJobHandler.getClass();
        SwakScheduled swakJob = extensionClz.getDeclaredAnnotation(SwakScheduled.class);
        if (swakJob != null) {
            String[] cron = swakJob.cron();
            if (ArrayUtils.isNotEmpty(cron)) {
                if (cron.length != swakJob.shardingCount()) {
                    Assert.state(cron.length != swakJob.shardingCount(), "sharding count illegal");
                }
                for (int i = 0; i < swakJob.shardingCount(); i++) {
                   String resolveCronStr = resolveAsString(cron[i]);
                    EasyJobInfo easyJobInfo = EasyJobInfo.builder()
                            .appName(easyJobConfig.getAppName())
                            .distributed(swakJob.distributed())
                            .shardingNum(i)
                            .shardingCount(swakJob.shardingCount())
                            .jobName(StringUtils.firstNonBlank(swakJob.jobName(), easyJobHandler.getClass().getSimpleName()) + i)
                            .scheduleConf(resolveCronStr)
                            .scheduleType(ScheduleType.CRON.getType())
                            .executorParam(Arrays.toString(swakJob.params()))
                            .executorHandler(beanName)
                            .scheduleEnabled(swakJob.enabled())
                            .triggerLastTime(System.currentTimeMillis())
                            .triggerNextTime(new CronExpression(resolveCronStr).getNextValidTimeAfter(new Date()).getTime())
                            .build();
                    taskList.add(easyJobInfo);
                }
                return taskList;
            }
            long[] fixedRate = swakJob.fixedRate();
            if (ArrayUtils.isNotEmpty(fixedRate)) {
                if (cron.length != swakJob.shardingCount()) {
                    Assert.state(fixedRate.length != swakJob.shardingCount(), "sharding count illegal");
                }
                for (int i = 0; i < swakJob.shardingCount(); i++) {
                    EasyJobInfo easyJobInfo = EasyJobInfo.builder()
                            .shardingNum(i)
                            .distributed(swakJob.distributed())
                            .shardingCount(swakJob.shardingCount())
                            .jobName(StringUtils.firstNonBlank(swakJob.jobName(), easyJobHandler.getClass().getSimpleName()) + i)
                            .scheduleConf(String.valueOf(swakJob.unit().toSeconds(fixedRate[i])))
                            .scheduleType(ScheduleType.FIX_RATE.getType())
                            .executorParam(Arrays.toString(swakJob.params()))
                            .executorHandler(beanName)
                            .scheduleEnabled(swakJob.enabled())
                            .triggerLastTime(System.currentTimeMillis())
                            .triggerNextTime(new Date(System.currentTimeMillis() + swakJob.unit().toMillis(fixedRate[i])).getTime())
                            .build();
                    taskList.add(easyJobInfo);
                }
            }
        }
        return taskList;
    }

    @Override
    public void afterSingletonsInstantiated() {
        registerJobLock();//注册锁
        registerByDistributed();//注册job
        jobScheduleHandler.start();//启动任务调度
    }

    private void registerJobLock() {
        easyJobMapper.initJobLockData("register_lock");
        easyJobMapper.initJobLockData("schedule_lock");
    }


    private boolean registerByDistributed() {
        Connection conn = null;
        Boolean connAutoCommit = null;
        PreparedStatement preparedStatement = null;
        boolean preReadSuc = true;
        try {
            conn = easyJobMapper.getJdbcTemplate().getDataSource().getConnection();
            connAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            preparedStatement = conn.prepareStatement(String.format("select * from %s where lock_name = 'register_lock' and app_name='%s' for update",
                    easyJobConfig.getJobLockTableName(), easyJobConfig.getAppName()));
            preparedStatement.execute();
            //注册，获取注解的定时任务,区分是否分布式
            List<EasyJobInfo> easyJobInfos = doRegistration().stream().filter(item -> item.isDistributed()).collect(Collectors.toList());
            return easyJobMapper.register(easyJobInfos);
        } catch (Exception e) {
            log.error("register job infos error,message:" + e.getMessage(), e);
        } finally {
            // commit
            if (conn != null) {
                try {
                    conn.commit();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
                try {
                    conn.setAutoCommit(connAutoCommit);
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }

            // close PreparedStatement
            if (null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return false;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        doRegistration().stream().filter(item -> !item.isDistributed()).forEach(jobInfo -> {
            EasyJobTask easyJobTask = new EasyJobTask(jobInfo, jobScheduleHandler);
            if (ScheduleType.CRON.match(jobInfo.getScheduleType())) {
                taskRegistrar.addTriggerTask(easyJobTask, new CronTrigger(jobInfo.getScheduleConf()));
            }
            //保存为秒 需要*1000
            if (ScheduleType.FIX_RATE.match(jobInfo.getScheduleType())) {
                taskRegistrar.addFixedDelayTask(easyJobTask, GetterUtil.getLong(jobInfo.getScheduleConf()) * 1000);
            }
        });
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.resolver = ((ConfigurableListableBeanFactory) beanFactory).getBeanExpressionResolver();
            this.expressionContext = new BeanExpressionContext((ConfigurableListableBeanFactory) beanFactory, listenerScope);
        }
    }

    /**
     * Resolve expression object.
     *
     * @param value the value
     * @return the object
     */
    public Object resolveExpression(String value) {
        return this.resolver.evaluate(resolve(value), this.expressionContext);
    }

    /**
     * 解析 ${}
     *
     * @param value
     * @return
     */
    private String resolve(String value) {
        if (this.beanFactory != null && this.beanFactory instanceof ConfigurableBeanFactory) {
            return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
        }
        return value;
    }


    /**
     * Resolve expression as boolean.
     *
     * @param value the value
     * @return the boolean
     */
    public Boolean resolveAsBoolean(String value) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        Object resolved = resolveExpression(value);
        Boolean result = null;
        if (resolved instanceof Boolean) {
            result = (Boolean) resolved;
        } else if (resolved instanceof String) {
            result = Boolean.parseBoolean((String) resolved);
        } else if (resolved != null) {
            throw new IllegalStateException(
                    "The must resolve to a Boolean or a String that can be parsed as a Boolean. "
                            + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
        }
        return result;
    }

    public String resolveAsString(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        Object resolved = resolveExpression(value);
        if(Objects.isNull(resolved)){
            return value;
        }
        if(resolved instanceof  String){
            return (String) resolved;
        }
        return resolved.toString();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.resolver = new StandardBeanExpressionResolver(classLoader);
    }

    @Override
    public void setEnvironment(Environment environment) {

    }

    private static class ListenerJobScope implements Scope {
        private final Map<String, Object> listeners = new HashMap<>();

        /**
         * Instantiates a new Listener job scope.
         */
        ListenerJobScope() {
            super();
        }

        /**
         * Add listener.
         *
         * @param key  the key
         * @param bean the bean
         */
        public void addListener(String key, Object bean) {
            this.listeners.put(key, bean);
        }

        /**
         * Remove listener.
         *
         * @param key the key
         */
        public void removeListener(String key) {
            this.listeners.remove(key);
        }

        @Override
        public Object get(String name, ObjectFactory<?> objectFactory) {
            Object scopedObject = this.listeners.get(name);
            if(scopedObject ==null){
                scopedObject = objectFactory.getObject();
                addListener(name,scopedObject);
            }
            return scopedObject;
        }

        @Override
        public Object remove(String name) {
            Object scopedObject = this.listeners.get(name);
            if(scopedObject!=null){
                removeListener(name);
                return scopedObject;
            }
            return null;
        }

        @Override
        public void registerDestructionCallback(String name, Runnable callback) {
        }

        @Override
        public Object resolveContextualObject(String key) {
            return this.listeners.get(key);
        }

        @Override
        public String getConversationId() {
            return null;
        }
    }
}
