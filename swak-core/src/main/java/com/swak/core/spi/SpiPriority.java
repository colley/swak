package com.swak.core.spi;

import com.swak.core.SwakConstants;

/**
 * Spi实现的优先级接口 数字越小优先级越高
 * @author colley.ma
 * @since 3.0.0
 */
public interface SpiPriority {

    default int priority() {
        return SwakConstants.SPI_PRIORITY;
    }
}
