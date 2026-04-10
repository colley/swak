package com.swak.core.seq;

import com.swak.common.util.StringPool;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.ConcurrentHashMap;

/**
 * compressed id generator, result id not 53bits before 2318-06-04.
 */

public class SwakDefaultIdGenerator implements IdGenerator {

    private static ConcurrentHashMap<String, IdGeneratorFactory> Factory = new ConcurrentHashMap<>();


    // total bits=53(max 2^53-1：9007199254740992-1)

    // private final static long TIME_BIT = 40; // max: 2318-06-04
    private final static long MACHINE_BIT = 5; // max 31
    private final static long SEQUENCE_BIT = 8; // 256/10ms

    /**
     * mask/max value
     */
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long TIMESTAMP_LEFT = MACHINE_BIT + SEQUENCE_BIT;

    private long machineId;
    private long sequence = 0L;
    private long lastStamp = -1L;


    /**
     * 获取 maxWorkerId
     */
    protected static long getMaxWorkerId(long datacenterId, long maxWorkerId) {
        StringBuilder mpd = new StringBuilder();
        mpd.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isNotEmpty(name)) {
            /*
             * GET jvmPid
             */
            mpd.append(name.split(StringPool.AT)[0]);
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (mpd.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    /**
     * 数据标识id部分
     */
    protected static long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 1]) | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                    id = id % (maxDatacenterId + 1);
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return id;
    }

    private SwakDefaultIdGenerator(long machineId) {
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException(
                    "machineId can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.machineId = machineId;
    }

    SwakDefaultIdGenerator() {
        this.machineId =  getMaxWorkerId(getDatacenterId(MAX_MACHINE_NUM),MAX_MACHINE_NUM);
    }

    /**
     * generate new ID
     *
     */
    @Override
    public synchronized Long nextId() {
        long currStmp = getTimestamp();
        if (currStmp < lastStamp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                currStmp = getNextTimestamp();
            }
        } else {
            sequence = 0L;
        }

        lastStamp = currStmp;

        return currStmp << TIMESTAMP_LEFT //
                | machineId << MACHINE_LEFT //
                | sequence;
    }

    private long getNextTimestamp() {
        long mill = getTimestamp();
        while (mill <= lastStamp) {
            mill = getTimestamp();
        }
        return mill;
    }

    private long getTimestamp() {
        // per 10ms
        return System.currentTimeMillis() / 10;// 10ms
    }



}
