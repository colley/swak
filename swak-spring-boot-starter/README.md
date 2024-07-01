# SWAK使用说明手册

## SWAK概要

SWAK架构基于 Spring Boot2构建微服务架构，集成通用的基础技术模块和业务模块，快速搭建可直接部署。统一的接口出入参和公共规范，支持多环境多机房，支持多业务多场景扩展点，支持容错，限流，隔离，兜底，支持接口监控业务监控。集成通用工具类，支持白名单，穿越预演能力。

### Latest Version: [![Maven Central](https://img.shields.io/maven-central/v/io.gitee.mcolley/swak-core.svg)](https://search.maven.org/search?q=g:io.gitee.mcolley%20a:swak*)

```xml
 <dependency>
    <groupId>io.gitee.mcolley</groupId>
    <artifactId>swak-bom</artifactId>
    <version>Latest Version</version>
    <type>pom</type>
    <scope>import</scope>
 </dependency>

 <dependency>
    <groupId>io.gitee.mcolley</groupId>
     <artifactId>swak-spring-boot-starter</artifactId>
 </dependency>

```

### 依赖中间件版本


| 中间件名称    | 版本           | 备注        |
| ------------- | -------------- | ----------- |
| `spring boot` | 2.3.12.RELEASE | spring boot |

### 使用说明文档

#### 一、实体类定义

1. vo层继承com.swak.frame.dto.base.VO 命名规则 xxVo
2. 数据库Entity继承com.swak.frame.dto.base.Entity 命名规则 xxEntity
3. 查询等入参实体继承Query,命名规则 xxReq/xxQuery
4. 分页继承PageInfo，分页返回统一的数据结构Pagination
   ```java
   private Listt list;
   private PageInfo page;
   ```
5. 有更新操作的请求继承Command，命名规则 xxCommand/xxCmd

#### 二、返回数据结构

1. 通使用Response返回统一的数据结构。

```java
  private Integer code;
  private String msg;
  private T data;
  //使用方式
  Response.success(xx);
  Response.fail(IResultCode)
```

2. 使用统一的相应code，所有定义的code必须实现IResultCode接口，可以用于返回msg的国际化。

```java
   public enum BasicErrCode implements IResultCode {
   SUCCESS(0, "请求成功~"),
   USER_NO_LOGIN(401, "您未登录，请先登录~"),
   SWAK_ERROR(900, "系统异常，稍后再试~"),
   BIZ_ERROR(902, "业务异常，请稍后再试~"),
   INVALID_PARAMETER(903, "您输入的参数异常~"),
   ......
```

3. 定义枚举必须继承EnumType，可以用于入参校验以及国际化。
   ```java
      @EnumValid(value = ThresholdCalcType.class,notEmpty = true)
      private String calcType;
   ```

#### 三、多场景扩展点

根据多场景的情况增加扩展点：扩展点表示一块逻辑在不同的业务有不同的实现，使用扩展点做接口申明,
扩展点声明接口：ExtensionPoint 使用@Extension设置不容实现的优先级和多场景标签 扩展的服务需要继承扩展点声明接口ExtensionPoint。

```java

	@Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    @Component
    public @interface Extension {
        //业务编码
        String bizId() default BizScenario.DEFAULT_BIZ_ID;
        //case
        String useCase() default BizScenario.DEFAULT_USE_CASE;
        //场景
        String scenario() default BizScenario.DEFAULT_SCENARIO;
    }
```

通过ExtensionExecutor执行扩展点服务：
自动计算UCL例子：

```java
   @Extension(bizId = StatisticExtPtCalculate.BIZ_ID, useCase = "TARGET_CONSTANT")
   public class TargetConstantCalculate implements StatisticExtPtCalculate {
      @Override
      public Double calcAboveValue(StatDetectionDataAvgAndStdVo source, CalcTypeValue calcValue) {
         Double target = source.getTarget();
         if (Objects.isNull(target) || target.isNaN()) {
            return null;
         }
         ....
      }

   BizScenario bizScenario = BizScenario.valueOf(StatisticExtPtCalculate.BIZ_ID, conf.getType());
     Double ucl = extensionExecutor.execute(StatisticExtPtCalculate.class, bizScenario,
   extPt -> extPt.calcBelowValue(avgAndStd,conf));
```

#### 四、excel导入导出

封装了easyExcel，统一表头样式和导入提供统一的校验

通过Spring Boot的autoConfig机制进行加载，无需手动配置，只需要添加如下依赖即可：

```xml
       	<dependency>
            <groupId>io.gitee.mcolley</groupId>
            <artifactId>swak-excel-boot-starter</artifactId>
        </dependency>
```

- 导出Excel

```java
        ExcelWriteData<UserExcel> excelWriteData = new ExcelWriteData<>();
        excelWriteData.setData(new ArrayList<>());
        for(int i=0;i<50;i++) {
        excelWriteData.getData().add(new UserExcel("colley"+i,i));
        }
        ExcelWriteHandler excelWriteHandler =  ExcelWriteHandler.write(outputStream,UserExcel.class);
        excelWriteHandler.writeExcelParams(new WriteExcelParams()).doWrite(excelWriteData);
```

- 动态设置表头，使用占位符 $ {xx} 方式实现动态表头

```java

    @Data
    @AllArgsConstructor
    public class UserExcel extends BaseRow {
        @ExcelProperty(value = "${name}")
        private String name;
        ......
    }
 Map<String,String> dynamicTitleHeader = Maps.newHashMap();
        dynamicTitleHeader.put("name","Swak测试动态title");
  
  excelWriteHandler.dynamicTitleHeader(dynamicTitleHeader).doWrite(excelWriteData);
```

- 导出多个sheet

```java
  List<ExcelWriteData<?>> excelWriteDataList = Lists.newArrayList();
  excelWriteDataList.add(new ExcelWriteData(sheetName));
  excelWriteHandler.head(Class<?>... headClazzArray).doWrite(excelWriteDataList);
```

##### 导入Excel

- 单个sheetName导入

```java
ExcelReadHandler<UserExcel> excelReadHandler = ExcelReadHandler.read(file);
return excelReadHandler.head(UserExcel.class)
.readExcelParams(new ReadExcelParams())
.addBizValidator(item->dictionaryService.validate(command, item)) //业务逻辑检验
.doRead((sheetData)->dictionaryService.importRegister(command, sheetData)); //导入逻辑
```

- 多个sheetName导入

#### 五、嵌入式任务调度

嵌入式任务调度，采用数据库锁的方式实现任务调度，支持分布式和本地调度。支持配置中心设置开关

```xml
        <dependency>
            <groupId>io.gitee.mcolley</groupId>
            <artifactId>swak-easyjob-boot-starter</artifactId>
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

通过使用SwakScheduled和 job实现EasyJobHandler接口

例子：

```java

@Slf4j
@SwakScheduled(cron ="0/5 * * * * ?",distributed = true,enabled = "${easy.job.user.enabled:true}")
public class HelloJob implements EasyJobHandler {
  
    @Override
    public EasyResult<Void> execute(EasyJobContext context) {
        log.warn("hello easy job>>>>>>>>>>>>>>>>>>>>>>>");
        return EasyResult.SUCCESS;
    }
}

```

##### 初始化启动任务调度

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

#### 五、其它功能使用

- 增加基于 spring的RestTemplate的封装SwakRestTemplate，默认OKHttpClient

```java
SwakRestTemplate restTemplate = RestTemplateBuilder.newBuilder().setConnectTimeout(1000)
.setReadTimeout(1000).setWriteTimeout(2000).build();
JSONObject resultImpl = restTemplate.postRequest(url,uriVariables,JSONObject.class);
```

- 增加monitor监控（内存监控、内存分代监控、系统运行时数据监控、垃圾回收已经nioBuffer等）
- 基 于Eventbus机制的异步处理方案。（分异步和同步等）
- 基于spring async异步封装。增加统一的线程池来实现spring async和增加线程池的监控。
- 提供@ExtCacheable多级缓存方案。
- 基于sql的ID生产器方案。
- 基于sso+shrio的权限控制解决方案。
