
package com.swak.core.aware;

public interface MonitorAware {

    void startup();

    default long getMonitorPeriod() {
        return 120L;
    };

    default boolean enabled() {
        return true;
    }
}
