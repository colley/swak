
package com.swak.core.monitor.system;

import com.swak.core.monitor.model.Garbage;
import com.swak.core.monitor.tools.JmxTools;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 类回收情况
 * 
 * @author liuliang8
 *
 */
public class GarbageMonitor {

    /**
     * 垃圾回收情况
     */
    public static List<Garbage> garbages() throws Exception {

        List<Garbage> list = new ArrayList<Garbage>();
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectInstance> memoryOPools =
            mbeanServer.queryMBeans(new ObjectName("java.lang:type=GarbageCollector,*"), null);

        for (ObjectInstance oi : memoryOPools) {
            ObjectName objName = oi.getObjectName();
            Garbage garbage = new Garbage();
            garbage
                .setCollectionCount(JmxTools.getLongAttr(mbeanServer, objName, "CollectionCount"));
            garbage.setCollectionTime(JmxTools.getLongAttr(mbeanServer, objName, "CollectionTime"));
            garbage.setName(JmxTools.getStringAttr(mbeanServer, objName, "Name"));
            garbage
                .setMemoryPoolNames(JmxTools.getArrayAttr(mbeanServer, objName, "MemoryPoolNames"));

            list.add(garbage);
        }
        return list;

    }



}
