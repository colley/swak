package com.swak.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author colley.ma
 * @since 3.0.0
 **/
@Data
@ConfigurationProperties(prefix = "spring.redis.redisson")
public class RedissonProperties {
    private String config;
    private Boolean enabled;
    private String file;
}