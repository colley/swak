package com.swak.common.spi;

/**
 * Spi实现的优先级接口 数字越小优先级越高
 *
 * @author colley.ma
 * @since 3.0.0
 */
public abstract class AbstractSpiPriority implements SpiPriority{

    private int priority = SpiPriority.SPI_PRIORITY;

    private String name = SpiPriority.SPI_NAME;

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
   public  int priority() {
        return this.priority;
    }


    @Override
    public  String getName() {
        return this.name;
    }
}
