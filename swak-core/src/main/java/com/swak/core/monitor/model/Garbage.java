package com.swak.core.monitor.model;


/**
 * 垃圾回收信息
 * 
 * @author liuliang8
 *
 */
public class Garbage {

    private long collectionCount;

    private long collectionTime;

    private String name;

    private String[] memoryPoolNames;

    public Garbage() {

    }

    public long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getMemoryPoolNames() {
        return memoryPoolNames;
    }

    public void setMemoryPoolNames(String[] memoryPoolNames) {
        this.memoryPoolNames = memoryPoolNames;
    }



}
