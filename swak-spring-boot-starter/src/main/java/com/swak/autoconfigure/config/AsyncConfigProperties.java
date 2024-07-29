package com.swak.autoconfigure.config;

/**
 * CustomAsyncProperties.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface AsyncConfigProperties {

    Integer getCorePoolSize();

    Integer getMaxPoolSize();

    Integer getQueueCapacity();
}
