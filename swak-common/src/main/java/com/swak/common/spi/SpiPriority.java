package com.swak.common.spi;

/**
 * Spi实现的优先级接口 数字越小优先级越高
 *
 * @author colley.ma
 * @since 3.0.0
 */
public interface SpiPriority {

    int SPI_PRIORITY = 100;

    default int priority() {
        return SPI_PRIORITY;
    }
}
