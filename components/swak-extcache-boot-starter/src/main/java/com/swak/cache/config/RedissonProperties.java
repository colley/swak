package com.swak.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author colley.ma
 * @since 3.0.0
 **/
@ConfigurationProperties(prefix = "spring.redis.redisson")
public class RedissonProperties {
    private String config;

    private String file;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}