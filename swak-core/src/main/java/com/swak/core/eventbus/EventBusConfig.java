package com.swak.core.eventbus;

import lombok.Data;

@Data
public class EventBusConfig {
    private Integer CorePoolSize;
    private Integer maxPoolSize;
    private Integer queueCapacity;

    public static  EventBusConfig defaultConf() {
        EventBusConfig config = new EventBusConfig();
        config.setCorePoolSize(2);
        config.setMaxPoolSize(10);
        config.setQueueCapacity(500);
        return config;
    }
}
