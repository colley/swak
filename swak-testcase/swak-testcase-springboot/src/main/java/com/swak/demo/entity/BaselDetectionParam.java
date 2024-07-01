package com.swak.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class BaselDetectionParam {

    @TableField(exist = false)
    private Long id;

    @TableField(exist = false)
    private Long detectParamId;

    @TableField(value = "version")
    private Integer version;

    @TableField(value = "window_id")
    private  Long windowId;

    @TableField(value = "window_type")
    private  String windowType;

    @TableField(value = "model_process_id")
    private  Long modelProcessId;

    @TableField(value = "summary_type")
    private  String summaryType;

    @TableField(value = "parameter_id")
    private  Long parameterId;

    @TableField(value = "parameter_name")
    private String parameterName;

    @TableField(value = "unique_code")
    private String uniqueCode;

    @TableField(value = "filter_data")
    private String filterData;

    @TableField(value = "filter_threshold")
    private String filterThreshold;

    @TableField(value = "filter_condition")
    private String filterCondition;


    @TableField(value = "calc_mode")
    private String calcMode;


    @TableField(value = "expression")
    private String expression;

    @TableField(value = "summary_conf")
    private String summaryConf;

    @TableField(value = "custom_algorithm")
    private String customAlgorithm;

    @TableField(value = "strategy_rule")
    private String strategyRule;

    @TableField(value = "strategy_type")
    private String strategyType;

    @TableField(value = "creator_id")
    private String creatorId;

    @TableField(value = "create_time")
    private Date createTime;
}
