package com.swak.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;


@Data
public class BaseDetectionRule {

    @TableField(exist = false)
    private Long id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "version")
    private Integer version;

    @TableField(value = "detect_param_id")
    private Long detectParamId;

    @TableField(value = "window_id")
    private  Long windowId;

    @TableField(value = "model_process_id")
    private  Long modelProcessId;

    @TableField(value = "detect_mode")
    private Integer  detectMode;

    @TableField(value = "is_detection_enable")
    private Integer isDetectionEnable;

    @TableField(value = "is_data_quality_enable")
    private Integer isDataQualityEnable;

//    @TableField(value = "monitor_condition")
//    private String  monitorCondition;

    @TableField(value = "alarm_filter")
    private String alarmFilter;

    @TableField(value = "notice_filter")
    private String noticeFilter;

    @TableField(value = "tags")
    private String tags;

    @TableField(value = "threshold_type")
    private String  thresholdType;

    @TableField(value = "target_conf")
    private String targetConf;

    @TableField(value = "creator_id")
    private String creatorId;

    @TableField(value = "create_time")
    private Date createTime;
}
