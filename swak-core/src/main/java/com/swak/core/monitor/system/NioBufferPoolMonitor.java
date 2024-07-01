
package com.swak.core.monitor.system;

import com.swak.core.monitor.model.NioBufferPool;
import com.swak.core.monitor.tools.ByteToM;
import com.swak.core.monitor.tools.JmxTools;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 获取nio 内存情况
 * 
 * @author liuliang8
 *
 */
public class NioBufferPoolMonitor {

    /**
     * 获取类加载情况
     * 
     * @return
     * @throws Exception
     */
    public static List<NioBufferPool> bufferPools() throws Exception {

        List<NioBufferPool> pools = new ArrayList<>();
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectInstance> memoryOPools =
            mbeanServer.queryMBeans(new ObjectName("java.nio:type=BufferPool"), null);

        for (ObjectInstance oi : memoryOPools) {
            NioBufferPool pool = new NioBufferPool();
            ObjectName objName = oi.getObjectName();
            pool.setCount(JmxTools.getLongAttr(mbeanServer, objName, "Count"));
            pool.setMemoryUsed(
                ByteToM.convert(JmxTools.getLongAttr(mbeanServer, objName, "MemoryUsed")));
            pool.setName(JmxTools.getStringAttr(mbeanServer, objName, "Name"));
            pool.setTotalCapacity(
                ByteToM.convert(JmxTools.getLongAttr(mbeanServer, objName, "TotalCapacity")));
            pools.add(pool);
        }
        return pools;

    }


}
