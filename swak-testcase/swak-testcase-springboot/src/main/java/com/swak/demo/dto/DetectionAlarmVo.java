package com.swak.demo.dto;

import com.swak.common.dto.base.VO;
import lombok.Data;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/7 10:57
 */
@Data
public class DetectionAlarmVo implements VO {

    private Long detectParamId;

    private  Long windowId;

    private  Long modelProcessId;

    private Integer priority;

    private String alarmTag;

    private String conditionType;

    private String alarmRule;

    private  Long parameterId;

    private String parameterName;

    private String uniqueCode;

    private String filterData;
}
