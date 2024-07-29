package com.swak.demo.config;

import com.swak.autoconfigure.config.AsyncConfigProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Swakss.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "task.async")
public class SwakAsyncConfigProperties implements AsyncConfigProperties {

    private Integer corePoolSize;
    private Integer maxPoolSize;
    private Integer queueCapacity;

    @Override
    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    @Override
    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    @Override
    public Integer getQueueCapacity() {
        return queueCapacity;
    }
}
