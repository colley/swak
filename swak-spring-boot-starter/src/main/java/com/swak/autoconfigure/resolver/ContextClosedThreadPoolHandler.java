package com.swak.autoconfigure.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 线程连接池优雅关机
 * @author colley.ma
 * @date 2022/07/13 18:18:45
 *
 */
@Slf4j
public class ContextClosedThreadPoolHandler implements ApplicationListener<ContextClosedEvent>{

	
	private ThreadPoolTaskExecutor threadPool;

	
	@Override
	public void onApplicationEvent(ContextClosedEvent context) {
		shutdownAndAwaitTermination(threadPool.getThreadPoolExecutor());
	}
	
	private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitte\
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                	log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

	public void setThreadPool(ThreadPoolTaskExecutor threadPool) {
		this.threadPool = threadPool;
	}
}
