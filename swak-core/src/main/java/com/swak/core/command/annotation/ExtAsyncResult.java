/**
 * Copyright © 2022 SWAK Info.
 * File Name: SwkAsyncResult.java
 */
package com.swak.core.command.annotation;

import com.swak.core.command.closure.SwakCommand;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * 继承 spring的异步AsyncResult来扩展所有的异步请求
 *
 * <pre>
 * 	1、支持spring的AsyncResult
 *  2、支持hystrix的command.queue()异步
 *  3、支持限流的
 * </pre>
 *
 * @author colley.ma
 * @since  2022/01/20
 */
public class ExtAsyncResult<V> extends AsyncResult<V> implements SwakCommand<V> {

    private final Supplier<V> supplier;

    public ExtAsyncResult(V value) {
        super(value);
        this.supplier = null;
    }

    public ExtAsyncResult(Supplier<V> supplier) {
        super(null);
        this.supplier = supplier;
    }

    public static <V> ExtAsyncResult<V> supplyAsync(Supplier<V> supplier) {
        return new ExtAsyncResult<>(supplier);
    }

    public static <V> ExtAsyncResult<V> supplyAsync(V value) {
        return new ExtAsyncResult<>(value);
    }

    @Override
    public V get() throws ExecutionException {
        if (supplier != null) {
            return supplier.get();
        }
        return super.get();
    }

    @Override
    public CompletableFuture<V> completable() {
        if (supplier != null) {
            // 如果 supplier 不为空走supplyAsync
            return CompletableFuture.supplyAsync(supplier);
        }
        /**
         * 其它情况，如spring的see org.springframework.scheduling.annotation.Async @Async
         */
        return super.completable();
    }

    @Override
    public V invoke() {
        return supplier.get();
    }
}
