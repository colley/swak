package com.swak.core.monitor.model;

/**
 * 类加载信息
 * 
 * @author liuliang8
 *
 */
public class ClassLoading {

    /**
     * 已经 加载的类数量
     */
    private int loadedClassCount;

    /**
     * 卸载的类数量
     */
    private long unloadedClassCount;

    /**
     * 总共加载类数量
     */
    private long totalLoadedClassCount;



    public ClassLoading() {

    }



    public int getLoadedClassCount() {
        return loadedClassCount;
    }



    public void setLoadedClassCount(int loadedClassCount) {
        this.loadedClassCount = loadedClassCount;
    }



    public long getUnloadedClassCount() {
        return unloadedClassCount;
    }



    public void setUnloadedClassCount(long unloadedClassCount) {
        this.unloadedClassCount = unloadedClassCount;
    }



    public long getTotalLoadedClassCount() {
        return totalLoadedClassCount;
    }



    public void setTotalLoadedClassCount(long totalLoadedClassCount) {
        this.totalLoadedClassCount = totalLoadedClassCount;
    }



}
