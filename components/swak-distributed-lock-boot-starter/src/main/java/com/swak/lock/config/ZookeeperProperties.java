package com.swak.lock.config;

import com.swak.core.SwakConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author colley.ma
 * @since 3.0.0
 */

@Data
@ConfigurationProperties(prefix = SwakConstants.SWAK_ZOOKEEPER)
public class ZookeeperProperties {
    /**
     * ip:port,ip:port
     */
    private String  connectionStr;
    /**
     * 重试次数
     */
    private int retryCount = 3;
    /**
     * 耗时 ms
     */
    private int elapsedTime = 3000;

    private int sessionTimeout =60000;

    private int connectionTimeout = 15000;

    private String namespace;

}
