
package com.swak.cache.annotation;

import java.util.concurrent.TimeUnit;

public @interface Redis {

	 /** a spring-bean name who type  including its sub-classes. */
    String useRedisTemplate() default "redisCacheProxy";
    
    /** expire-time (0 represent never expire) */
    int expireTime() default -1;
    
    /** unit for {@link Redis#expireTime()} */
    
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
