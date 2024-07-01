package com.swak.lock.annotation;

import com.swak.common.dto.base.BaseOperation;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

@Data
@Accessors(chain = true)
public class LockOperation implements BaseOperation {

    /**
     * timeToTry
     **/
    private Long timeToTry;

    /**
     * leaseTime
     */
    private Long leaseTime;

    /**
     * unit for expireTime
     */
    private TimeUnit timeUnit;

    private String key;

    private String fallbackMethod;
}
