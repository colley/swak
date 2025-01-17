package com.swak.operatelog;

import com.google.common.collect.Lists;
import com.lmax.disruptor.WorkHandler;
import com.swak.common.dto.CarrierValue;
import com.swak.common.exception.ThrowableWrapper;
import com.swak.common.timer.CycleTask;
import com.swak.common.timer.WheelTimerHolder;
import com.swak.operatelog.annotation.OperateDataLog;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author colley.ma 
 * @version v1.0
 * @since 2024/3/11 15:40
 **/
public class OperateLogEventHandler implements WorkHandler<CarrierValue<OperateDataLog>> {
    private static final int MAX_SIZE = 10;

    private static final int BATCH_SIZE = 500;

    private OperateLogService operateLogService;
    private ArrayBlockingQueue<OperateDataLog> queue = new ArrayBlockingQueue<>(1024);
    private volatile LogRecordTask logRecordTask;

    public OperateLogEventHandler(OperateLogService operateLogService) {
        this.operateLogService = operateLogService;
    }

    @Override
    public void onEvent(CarrierValue<OperateDataLog> event) throws Exception {
        if (Objects.isNull(logRecordTask)) {
            this.start();
        }
        List<OperateDataLog> result = Lists.newArrayList();
        if (queue.size() >= MAX_SIZE) {
            result.addAll(pollMessage());
        } else {
            queue.offer(event.getValue());
        }
        if (CollectionUtils.isNotEmpty(result)) {
            operateLogService.addOperationLogs(result);
        }
    }

    private List<OperateDataLog> pollMessage() {
        AtomicInteger index = new AtomicInteger(0);
        List<OperateDataLog> result = Lists.newArrayList();
        while (!queue.isEmpty() && index.incrementAndGet()<BATCH_SIZE) {
            OperateDataLog dataLog = queue.poll();
            if(Objects.isNull(dataLog)){
                break;
            }
            result.add(dataLog);
        }
        return result;
    }

    public void start() {
        this.logRecordTask = new LogRecordTask();
        this.logRecordTask.config(15, TimeUnit.SECONDS, true);
        WheelTimerHolder.refreshableWheel().newTimeout(logRecordTask, 15, TimeUnit.SECONDS);
    }

    public void close() {
        if (Objects.nonNull(logRecordTask)) {
            logRecordTask.cancel();
        }
        logRecordTask = null; //gc
    }

    private class LogRecordTask extends CycleTask {
        @Override
        protected void invoke() throws ThrowableWrapper {
            List<OperateDataLog> result = pollMessage();
            if (CollectionUtils.isNotEmpty(result)) {
                operateLogService.addOperationLogs(result); //主要批量更新，减少数据库操作
            }
        }
    }
}