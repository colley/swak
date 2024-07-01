package com.swak.license.server.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义需要校验的License参数
 */

@Data
public class LicenseCheckModel implements Serializable {
    /**
     * 可被允许的IP地址
     */
    private List<String> ipAddress;
    /**
     * 可被允许的MAC地址
     */
    private List<String> macAddress;
    private Integer processTypeAmount;
    private Integer eqpAmount;
    private Integer chamberAmount;
    private Integer gatherAmount;
    private Integer instanceAmount;
}
