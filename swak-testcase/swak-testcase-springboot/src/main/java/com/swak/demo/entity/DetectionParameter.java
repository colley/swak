package com.swak.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "detection_parameter_history")
public class DetectionParameter extends BaselDetectionParam {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "detect_param_id")
    private Long detectParamId;

    @TableField(value = "modifier_id")
    private String modifierId;
    @TableField(value = "modify_time")
    private Date modifyTime;

}
