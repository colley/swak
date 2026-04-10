package com.swak.autoconfigure.config;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * SwakAsyncConfigurer.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class SwakAsyncConfigurer implements AsyncConfigurer {

    private final Executor executor;
    private final AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler;

    public SwakAsyncConfigurer(Executor executor, AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler) {
        this.executor = executor;
        this.asyncUncaughtExceptionHandler = asyncUncaughtExceptionHandler;
    }

    public SwakAsyncConfigurer(Executor executor) {
        this(executor, new AsyncExceptionHandler());
    }

    @Override
    public Executor getAsyncExecutor() {
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return asyncUncaughtExceptionHandler;
    }

    /**
     * 自定义异常处理类
     */
    static class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
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
