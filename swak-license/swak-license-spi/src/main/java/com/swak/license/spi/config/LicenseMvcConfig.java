package com.swak.license.spi.config;


import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class LicenseMvcConfig {
    private Set<String> includePatterns = new HashSet<>();
    private  Set<String> excludePatterns = new HashSet<>();
}
