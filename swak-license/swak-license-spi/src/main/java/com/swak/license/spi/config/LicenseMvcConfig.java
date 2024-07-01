package com.swak.license.spi.config;


import lombok.Data;

@Data
public class LicenseMvcConfig {

    private String[] pathPatterns;


    private String[] excludePathPatterns;
}
