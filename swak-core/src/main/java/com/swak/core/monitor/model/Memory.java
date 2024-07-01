package com.swak.core.monitor.model;


/**
 * 内存监控
 * 
 * @author liuliang8
 *
 */
public class Memory {

    /**
     * 是否开启详细信息
     */
    private boolean verbose;

    /**
     * 对象终结数量
     */
    private int objectPendingFinalizationCount;

    /**
     * 堆内存情况
     */
    private MemoryItems heapMemoryUsage;

    /**
     * 非堆内存使用情况
     */
    private MemoryItems nonHeapMemoryUsage;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getObjectPendingFinalizationCount() {
        return objectPendingFinalizationCount;
    }

    public void setObjectPendingFinalizationCount(int objectPendingFinalizationCount) {
        this.objectPendingFinalizationCount = objectPendingFinalizationCount;
    }

    public MemoryItems getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public void setHeapMemoryUsage(MemoryItems heapMemoryUsage) {
        this.heapMemoryUsage = heapMemoryUsage;
    }

    public MemoryItems getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    public void setNonHeapMemoryUsage(MemoryItems nonHeapMemoryUsage) {
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }



}
