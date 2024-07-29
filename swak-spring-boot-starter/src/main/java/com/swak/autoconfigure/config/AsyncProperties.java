
package com.swak.autoconfigure.config;

import com.swak.common.util.GetterUtil;

/**
 * swak.threadpool.core_pool_size=20
 * swak.threadpool.max_pool_size=60
 * swak.threadpool.queue_capacity=100
 * <p>
 * AsyncProperties.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
public class AsyncProperties implements AsyncConfigProperties {
    private final static int DEFAULT_CORE_POOL_SIZE = 2;

    private final static int DEFAULT_MAX_POOL_SIZE = 15;

    private final static int DEFAULT_QUEUE_CAPACITY = 600;

    private Integer corePoolSize;
    private Integer maxPoolSize;
    private Integer queueCapacity;

    public AsyncProperties(){
        this.corePoolSize = DEFAULT_CORE_POOL_SIZE;
        this.maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        this.queueCapacity = DEFAULT_QUEUE_CAPACITY;
    }

    @Override
    public Integer getCorePoolSize() {
        if (GetterUtil.getInteger(corePoolSize) <= 0) {
            return DEFAULT_CORE_POOL_SIZE;
        }
        return corePoolSize;
    }

    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    @Override
    public Integer getMaxPoolSize() {
        if (GetterUtil.getInteger(maxPoolSize) <= 0) {
            return DEFAULT_MAX_POOL_SIZE;
        }
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    @Override
    public Integer getQueueCapacity() {
        return GetterUtil.getInteger(queueCapacity, DEFAULT_QUEUE_CAPACITY);
    }

    public void setQueueCapacity(Integer queueCapacity) {
        this.queueCapacity = queueCapacity;
    }
}
