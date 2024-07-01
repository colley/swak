package com.swak.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName(value = "detection_alarm_history")
public class DetectionAlarm extends BaseDetectionAlarm {

    @TableId(type = IdType.INPUT)
    private Long id;
}
