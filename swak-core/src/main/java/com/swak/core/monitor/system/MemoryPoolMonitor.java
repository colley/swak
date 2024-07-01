
package com.swak.core.monitor.system;


import com.swak.core.monitor.model.MemoryPool;
import com.swak.core.monitor.tools.JmxTools;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 内存池监控，监控 各个年代数据的内存使用情况
 * 
 * @author liuliang8
 *
 */
public class MemoryPoolMonitor {
    /**
     * 获取内存分代信息
     * 
     * @return
     * @throws Exception
     */
    public static List<MemoryPool> getPools() throws Exception {

        List<MemoryPool> memoryPools = new LinkedList<>();
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectInstance> memoryOPools =
            mbeanServer.queryMBeans(new ObjectName("java.lang:type=MemoryPool,*"), null);


        long totalInit = 0;
        long totalMax = 0;
        long totalUsed = 0;
        long totalCommitted = 0;

        for (ObjectInstance oi : memoryOPools) {
            ObjectName objName = oi.getObjectName();
            MemoryPool memoryPool = new MemoryPool();
            memoryPool.setName(JmxTools.getStringAttr(mbeanServer, objName, "Name"));
            memoryPool.setType(JmxTools.getStringAttr(mbeanServer, objName, "Type"));

            CompositeDataSupport cd =
                (CompositeDataSupport) mbeanServer.getAttribute(objName, "Usage");
            if (cd != null) {
                memoryPool.setMax(JmxTools.getLongAttr(cd, "max"));
                memoryPool.setUsed(JmxTools.getLongAttr(cd, "used"));
                memoryPool.setInit(JmxTools.getLongAttr(cd, "init"));
                memoryPool.setCommitted(JmxTools.getLongAttr(cd, "committed"));
            }

            totalInit += memoryPool.getInit();
            totalMax += memoryPool.getMax();
            totalUsed += memoryPool.getUsed();
            totalCommitted += memoryPool.getCommitted();

            memoryPools.add(memoryPool);
        }

        if (!memoryPools.isEmpty()) {
            MemoryPool pool = new MemoryPool();
            pool.setName("Total");
            pool.setType("TOTAL");
            pool.setInit(totalInit);
            pool.setUsed(totalUsed);
            pool.setMax(totalMax);
            pool.setCommitted(totalCommitted);
            memoryPools.add(pool);
        }

        return memoryPools;

    }
}
