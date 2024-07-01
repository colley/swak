package com.swak.lock.config;

import com.swak.core.SwakConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Data
@ConfigurationProperties(prefix = SwakConstants.SWAK_LOCK)
public class LockProperties {

    private boolean ignoreLockingExceptions;

    private String namespace = "lock";
}
