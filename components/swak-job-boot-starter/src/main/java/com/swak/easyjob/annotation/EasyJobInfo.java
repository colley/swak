package com.swak.easyjob.annotation;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class EasyJobInfo implements Serializable {
    private int id;
    private String appName;
    private String jobName;
    private Integer shardingCount;
    private Integer shardingNum;
    private String scheduleType;
    private String scheduleConf;
    private String executorHandler;
    private String executorParam;
    private long triggerLastTime;
    private long triggerNextTime;
    private boolean distributed;
    private String scheduleEnabled;
}
