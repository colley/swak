
package com.swak.core.aware;

import com.swak.common.listener.SwakEventListener;

public interface MonitorAware extends SwakEventListener {

    void startup();

    default long getMonitorPeriod() {
        return 120L;
    };

    default boolean enabled() {
        return true;
    }
}
