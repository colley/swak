package com.swak.autoconfigure.spring.configuration;


import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.swak.autoconfigure.config.AsyncProperties;
import com.swak.autoconfigure.resolver.ContextClosedThreadPoolHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync(proxyTargetClass = true)
@Slf4j
public class AsyncThreadPoolConfiguration implements AsyncConfigurer {

    @Autowired(required = false)
    private AsyncProperties asyncProperties;

    @Override
    @Bean("swakAsyncTaskExecutor")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        if (asyncProperties == null) {
            asyncProperties = new AsyncProperties();
        }
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(asyncProperties.getCorePoolSize());//当前线程数
        threadPool.setMaxPoolSize(asyncProperties.getMaxPoolSize());// 最大线程数
        threadPool.setQueueCapacity(asyncProperties.getQueueCapacity());
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        threadPool.setAwaitTerminationSeconds(60 * 10);// 等待时间 （默认为0，此时立即停止），并没等待xx秒后强制停止
        threadPool.setThreadNamePrefix("SwakAsync-threadPool-");
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        threadPool.initialize(); // 初始化
        return threadPool;
    }

    @Bean
    public ContextClosedThreadPoolHandler contextClosedHandler(
            @Qualifier("swakAsyncTaskExecutor") ThreadPoolTaskExecutor frameTaskExecutor) {
        ContextClosedThreadPoolHandler contextClosedHandler = new ContextClosedThreadPoolHandler();
        contextClosedHandler.setThreadPool(frameTaskExecutor);
        return contextClosedHandler;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }
    /**
     * 自定义异常处理类
     */
    class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        //手动处理捕获的异常
        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
            List<Object> params = Lists.newArrayList();
            for (Object param : obj) {
                params.add(param);
            }
            log.error("方法=" + method.getName(), throwable);
            log.error("方法名称为： - {}参数为：={}", method.getName(), JSON.toJSONString(params));
        }
    }

}
