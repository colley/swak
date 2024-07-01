package com.swak.cache.config;


import com.swak.core.SwakConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = SwakConstants.SWAK_CACHE)
public class SwakCacheProperties {

	private Boolean enabled;

	private String keyPrefix;

	private String ehcachePath;

}
