package com.swak.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "detection_rule_limits_history")
public class DetectionRule extends BaseDetectionRule {
    @TableId(type = IdType.AUTO)
    private Long id;
}
