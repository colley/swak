package com.swak.core.monitor.model;

/**
 * 内存项
 * 
 * @author liuliang8
 *
 */
public class MemoryItems {
    private String init;

    private String max;

    private String used;

    private String committed;

    public String getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getCommitted() {
        return committed;
    }

    public void setCommitted(String committed) {
        this.committed = committed;
    }



}
