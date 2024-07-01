
package com.swak.core.monitor.system;

import com.swak.core.monitor.model.ClassLoading;
import com.swak.core.monitor.tools.JmxTools;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Set;

/**
 * 类加载情况监控
 * 
 * @author liuliang8
 *
 */
public class ClassLoadingMonitor {

    /**
     * 获取类加载情况
     * 
     * @return
     * @throws Exception
     */
    public static ClassLoading classLoading() throws Exception {

        ClassLoading classLoading = new ClassLoading();
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectInstance> memoryOPools =
            mbeanServer.queryMBeans(new ObjectName("java.lang:type=ClassLoading"), null);

        for (ObjectInstance oi : memoryOPools) {
            ObjectName objName = oi.getObjectName();
            classLoading
                .setLoadedClassCount(JmxTools.getIntAttr(mbeanServer, objName, "LoadedClassCount"));
            classLoading.setUnloadedClassCount(
                JmxTools.getLongAttr(mbeanServer, objName, "UnloadedClassCount"));
            classLoading.setTotalLoadedClassCount(
                JmxTools.getLongAttr(mbeanServer, objName, "TotalLoadedClassCount"));

        }
        return classLoading;

    }

}
