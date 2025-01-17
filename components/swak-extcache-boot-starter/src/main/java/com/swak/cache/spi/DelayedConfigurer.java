package com.swak.cache.spi;

import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SpiServiceFactory;
import org.springframework.lang.Nullable;

import java.util.concurrent.Executor;

/**
 * DelayedConfigurer.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface DelayedConfigurer extends SpiPriority {

    @Nullable
    default Executor getDelayedExecutor() {
        return null;
    }

    static DelayedConfigurer getDelayedConfigurer() {
        return SpiServiceFactory.loadFirst(DelayedConfigurer.class);
    }
}
