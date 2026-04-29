package com.swak.license.spi.config;

import lombok.Data;

import java.util.List;

/**
 * LicenseExtraVo.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
@Data
public class LicenseCheckExtra implements java.io.Serializable{
    private List<String> ipAddress;
    private List<String> macAddress;
}
