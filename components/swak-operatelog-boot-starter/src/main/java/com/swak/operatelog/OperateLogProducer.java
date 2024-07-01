package com.swak.operatelog;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.swak.common.dto.CarrierValue;
import com.swak.common.util.NamedThreadFactory;
import com.swak.operatelog.annotation.OperateDataLog;
import com.swak.operatelog.annotation.OperateLogContext;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 *OperateLogProducer
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/11 15:19
 **/
@Slf4j
public class OperateLogProducer {
    public final static EventFactory<CarrierValue<OperateDataLog>> EVENT_FACTORY = () -> new CarrierValue<>();
    private static int maxActive = 2;
    private static int cacheRatio = 256;
    private final OperateLogEventHandler operateLogEventHandler;
    private final OperateLogService operateLogService;
    private Disruptor<CarrierValue<OperateDataLog>> disruptor;
    private RingBuffer<CarrierValue<OperateDataLog>> recordContainer;

    public OperateLogProducer(OperateLogService operateLogService) {
        this.operateLogService = operateLogService;
        this.operateLogEventHandler = new OperateLogEventHandler(operateLogService);
    }

    public void close() {
        this.disruptor.shutdown();
        operateLogEventHandler.close();
    }

    public void startWork() {
        //未开启日志记录
        if (!operateLogService.isOperateLogEnabled()) {
            return;
        }
        this.disruptor = new Disruptor<>(
                EVENT_FACTORY,
                BigDecimal.valueOf(2).pow(
                        BigDecimal.valueOf(Math.log(maxActive) / Math.log(2)).setScale(0, RoundingMode.CEILING).intValue()).intValue()
                        * cacheRatio, new NamedThreadFactory("operate-log", true));
        this.disruptor.handleEventsWithWorkerPool(operateLogEventHandler);
        this.recordContainer = disruptor.getRingBuffer();
        this.disruptor.start();
    }

    public void sendLogMessage(OperateDataLog message) {
        try {
            OperateLogContext contextHolder = new OperateLogContext();
            operateLogService.preHandle(contextHolder);
            message.setCreateTime(new Date());
            message.setUserId(contextHolder.getUserId());
            message.setContextHolder(contextHolder);
            long next = recordContainer.next();
            CarrierValue<OperateDataLog> eventContainer = recordContainer.get(next);
            eventContainer.setValue(message);
            recordContainer.publish(next);
        } catch (Throwable e) {
            log.error("LogEventProducer error", e);
        }
    }
}

