## 原理

嵌入式任务调度，采用数据库锁的方式实现任务调度，支持分布式和本地调度。支持配置中心设置开关

```xml
        <dependency>
            <groupId>io.gitee.mcolley</groupId>
            <artifactId>swak-easyjob-boot-starter</artifactId>
            <version>最新版本</version>
        </dependency>

```

支持分布式任务调度和兼容集成spring只有的调度。

```java

@Documented
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SwakScheduled {

    /**
     * 支持Spell表达式
     * @return
     */
    String  enabled() default  "";
    
    /**
     * 支持Spell表达式和配置中心配置
     */
    String enabled() default "";
    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";
    /**
     * Cron 表达式.
     */
    String[] cron() default {};
    /**
     * Fixed rate long [ ].
     */
    long[] fixedRate() default {};
    /**
     * Params string [ ].
     */
    String[] params() default {};
    /**
     * Unit time unit.
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
    /**
     * Job name string.
     */
    String jobName() default "";
    /**
     * 是否分布式执行
     */
    boolean distributed() default true;
    /**
     * Sharding count int.
     */
    int shardingCount() default 1;
}
        
 ```

## 使用介绍

通过使用SwakScheduled和 job实现EasyJobHandler接口

例子：

```java

@Slf4j
@SwakScheduled(cron ="0/5 * * * * ?",distributed = true,enabled = "${easy.job.user.enabled:true}")
public class HelloJob implements EasyJobHandler {
    
    @Override
    public Response<Void> execute(EasyJobContext context) {
        log.warn("hello easy job>>>>>>>>>>>>>>>>>>>>>>>");
        return EasyResult.SUCCESS;
    }
}

 ```

### 初始化启动任务调度

- EasyJobConfig说明
```java

public class EasyJobConfig {
    private String appName;
    private JdbcTemplate jdbcTemplate;
    private String jobInfoTableName = "easy_job_info";
    private String jobLockTableName = "easy_job_lock";

}

```

- 配置文件实例化

```java
/**
 * easyJob配置
 * @param jdbcTemplate
 * @return
 */
@Bean
public EasyJobConfig easyJobConfig(JdbcTemplate jdbcTemplate) {
        EasyJobConfig easyJobConfig =  new EasyJobConfig();
        easyJobConfig.setJdbcTemplate(jdbcTemplate);
        easyJobConfig.setAppName("appname");
        return easyJobConfig;
        }

 ```

- Mysql脚本

```sql
CREATE TABLE `easy_job_info` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
     `app_name` varchar(50) DEFAULT NULL COMMENT '应用名称',
     `job_name` varchar(80) NOT NULL COMMENT 'job名称',
     `schedule_type` varchar(80) NOT NULL COMMENT '调度类型',
     `schedule_conf` varchar(128) NOT NULL COMMENT '调度配置',
     `executor_handler` varchar(255) NOT NULL COMMENT '执行器任务handler',
     `sharding_num` int(11) DEFAULT NULL COMMENT '分片序号',
     `sharding_count` int(11) DEFAULT NULL COMMENT '分片数',
     `schedule_enabled` varchar(255) DEFAULT NULL COMMENT '开关配置，支持配置中心配置',
     `executor_param` varchar(512) DEFAULT NULL COMMENT '执行器任务参数',
     `trigger_last_time` bigint(13) NOT NULL COMMENT '上次调度时间',
     `trigger_next_time` bigint(13) NOT NULL COMMENT '下次调度时间',
     PRIMARY KEY (`id`) USING BTREE,
     UNIQUE KEY `uniq_job_appname` (`job_name`,`app_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='EasyJob信息表';

CREATE TABLE `easy_job_lock` (
     `app_name` varchar(50) NOT NULL COMMENT '应用名称',
     `lock_name` varchar(50) NOT NULL COMMENT '锁名称',
     PRIMARY KEY (`lock_name`,`app_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='job锁表';

 ```

## 历史版本功能