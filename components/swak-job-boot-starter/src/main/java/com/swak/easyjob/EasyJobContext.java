package com.swak.easyjob;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * The type Easy job context.
 *
 * @author colley
 */
@Data
@Builder
public class EasyJobContext implements Serializable {

    private String[] params;

    private Integer shardingCount;

    private String jobName;

    private Integer shardingNum;

    private EasyJobHandler executorHandler;
    private String scheduleType;
    private String scheduleConf;

}
