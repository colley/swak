package com.swak.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;


@Data
public class BaseDetectionAlarm {

    @TableField(exist = false)
    private Long id;

    @TableField(exist = false)
    private Long alarmId;

    @TableField(value = "version")
    private Integer version;

    @TableField(value = "detect_param_id")
    private Long detectParamId;

    @TableField(value = "window_id")
    private  Long windowId;

    @TableField(value = "model_process_id")
    private  Long modelProcessId;

    @TableField(value = "priority")
    private Integer priority;

    @TableField(value = "alarm_tag")
    private String alarmTag;

    @TableField(value = "condition_type")
    private String conditionType;

    @TableField(value = "alarm_rule")
    private String alarmRule;

//    @TableField(value = "email_conf")
//    private String emailConf;

    @TableField(value = "alarm_notice")
    private String alarmNotice;


    @TableField(value = "interlock")
    private String interlock;

    @TableField(value = "ext")
    private String ext;

    @TableField(value = "creator_id")
    private String creatorId;

    @TableField(value = "create_time")
    private Date createTime;
}
