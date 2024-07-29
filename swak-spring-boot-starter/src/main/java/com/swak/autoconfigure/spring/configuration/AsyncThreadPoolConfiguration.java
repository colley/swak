package com.swak.autoconfigure.spring.configuration;


import com.swak.autoconfigure.config.AsyncConfigProperties;
import com.swak.autoconfigure.config.SwakAsyncConfigurer;
import com.swak.autoconfigure.resolver.ContextClosedThreadPoolHandler;
import com.swak.common.exception.SwakException;
import com.swak.common.util.GetterUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnBean(AsyncConfigProperties.class)
@EnableAsync(proxyTargetClass = true)
public class AsyncThreadPoolConfiguration {

    private AsyncConfigProperties asyncConfigProperties;

    @Autowired(required = false)
    void setConfigurers(ObjectProvider<AsyncConfigProperties> configurers) {
        List<AsyncConfigProperties> candidates = configurers.stream().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(candidates)) {
            this.asyncConfigProperties = candidates.get(0);
        }
    }

    @Bean("swakAsyncTaskExecutor")
    @ConditionalOnBean(AsyncConfigProperties.class)
    public ThreadPoolTaskExecutor swakAsyncTaskExecutor() {
        Integer corePoolSize = GetterUtil.getInteger(asyncConfigProperties.getCorePoolSize());
        Integer maximumPoolSize = GetterUtil.getInteger(asyncConfigProperties.getMaxPoolSize());
        if (corePoolSize < 0 ||
                maximumPoolSize <= 0 ||
                maximumPoolSize < corePoolSize) {
            throw new SwakException("[AsyncConfigProperties]corePoolSize or maxPoolSize must gt 0 and maxPoolSize > corePoolSize");
        }
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(asyncConfigProperties.getCorePoolSize());//当前线程数
        threadPool.setMaxPoolSize(asyncConfigProperties.getMaxPoolSize());// 最大线程数
        threadPool.setQueueCapacity(asyncConfigProperties.getQueueCapacity());
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        threadPool.setAwaitTerminationSeconds(60 * 10);// 等待时间 （默认为0，此时立即停止），并没等待xx秒后强制停止
        threadPool.setThreadNamePrefix("SwakAsync-threadPool-");
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        threadPool.initialize(); // 初始化
        return threadPool;
    }

    @Bean
    @ConditionalOnBean(ThreadPoolTaskExecutor.class)
    public SwakAsyncConfigurer swakAsyncConfigurer(ThreadPoolTaskExecutor swakAsyncTaskExecutor) {
        return new SwakAsyncConfigurer(swakAsyncTaskExecutor);
    }

    @Bean
    @ConditionalOnBean(ThreadPoolTaskExecutor.class)
    public ContextClosedThreadPoolHandler contextClosedHandler(ThreadPoolTaskExecutor swakAsyncTaskExecutor) {
        ContextClosedThreadPoolHandler contextClosedHandler = new ContextClosedThreadPoolHandler();
        contextClosedHandler.setThreadPool(swakAsyncTaskExecutor);
        return contextClosedHandler;
    }

}
