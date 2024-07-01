
package com.swak.core.monitor.system;


import com.swak.core.monitor.model.Memory;
import com.swak.core.monitor.model.MemoryItems;
import com.swak.core.monitor.tools.ByteToM;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.management.ManagementFactory;
import java.util.Set;


/**
 * 具体内存监控，监控堆区和非堆区内存使用情况
 * 
 * @author liuliang8
 *
 */
public class MemoryMonitor {


    /**
     * 获取内存使用情况
     * 
     * @return
     * @throws Exception
     */
    public static Memory getMemory() throws Exception {

        Memory memory = new Memory();
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectInstance> memoryOPools =
            mbeanServer.queryMBeans(new ObjectName("java.lang:type=Memory"), null);

        for (ObjectInstance oi : memoryOPools) {
            ObjectName objName = oi.getObjectName();
            memory.setVerbose((Boolean) mbeanServer.getAttribute(objName, "Verbose"));
            memory.setObjectPendingFinalizationCount(
                (Integer) mbeanServer.getAttribute(objName, "ObjectPendingFinalizationCount"));
            CompositeDataSupport cd =
                (CompositeDataSupport) mbeanServer.getAttribute(objName, "HeapMemoryUsage");
            if (cd != null) {
                MemoryItems items = new MemoryItems();
                items.setMax(ByteToM.convert(cd, "max"));
                items.setUsed(ByteToM.convert(cd, "used"));
                items.setInit(ByteToM.convert(cd, "init"));
                items.setCommitted(ByteToM.convert(cd, "committed"));
                memory.setHeapMemoryUsage(items);
            }
            CompositeDataSupport cds =
                (CompositeDataSupport) mbeanServer.getAttribute(objName, "NonHeapMemoryUsage");
            if (cds != null) {
                MemoryItems items = new MemoryItems();
                items.setMax(ByteToM.convert(cds, "max"));
                items.setUsed(ByteToM.convert(cds, "used"));
                items.setInit(ByteToM.convert(cds, "init"));
                items.setCommitted(ByteToM.convert(cds, "committed"));
                memory.setNonHeapMemoryUsage(items);
            }
        }
        return memory;

    }

}
