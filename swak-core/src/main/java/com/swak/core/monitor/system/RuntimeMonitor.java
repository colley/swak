
package com.swak.core.monitor.system;

import com.swak.core.monitor.model.RuntimeInformation;
import com.swak.core.monitor.tools.JmxTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * 获取运行时 数据
 * 
 * @author liuliang8
 *
 */
public class RuntimeMonitor {

    private static Logger logger = LoggerFactory.getLogger(RuntimeMonitor.class);


    /**
     * 获取运行时数据
     */
    public static RuntimeInformation getRuntimeInformation() throws Exception {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        RuntimeInformation ri = new RuntimeInformation();

        try {
            ObjectName runtimeOName = new ObjectName("java.lang:type=Runtime");
            ri.setStartTime(JmxTools.getLongAttr(mbeanServer, runtimeOName, "StartTime"));
            ri.setUptime(JmxTools.getLongAttr(mbeanServer, runtimeOName, "Uptime"));
            ri.setVmVendor(JmxTools.getStringAttr(mbeanServer, runtimeOName, "VmVendor"));

            ObjectName osOName = new ObjectName("java.lang:type=OperatingSystem");
            ri.setOsName(JmxTools.getStringAttr(mbeanServer, osOName, "Name"));
            ri.setOsVersion(JmxTools.getStringAttr(mbeanServer, osOName, "Version"));

            if (!ri.getVmVendor().startsWith("IBM Corporation")) {
                ri.setTotalPhysicalMemorySize(
                    JmxTools.getLongAttr(mbeanServer, osOName, "TotalPhysicalMemorySize"));
                ri.setCommittedVirtualMemorySize(
                    JmxTools.getLongAttr(mbeanServer, osOName, "CommittedVirtualMemorySize"));
                ri.setFreePhysicalMemorySize(
                    JmxTools.getLongAttr(mbeanServer, osOName, "FreePhysicalMemorySize"));
                ri.setFreeSwapSpaceSize(
                    JmxTools.getLongAttr(mbeanServer, osOName, "FreeSwapSpaceSize"));
                ri.setTotalSwapSpaceSize(
                    JmxTools.getLongAttr(mbeanServer, osOName, "TotalSwapSpaceSize"));
                ri.setProcessCpuTime(JmxTools.getLongAttr(mbeanServer, osOName, "ProcessCpuTime"));
                ri.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
            } else {
                ri.setTotalPhysicalMemorySize(
                    JmxTools.getLongAttr(mbeanServer, osOName, "TotalPhysicalMemory"));
            }

            if (JmxTools.hasAttribute(mbeanServer, osOName, "OpenFileDescriptorCount")
                && JmxTools.hasAttribute(mbeanServer, osOName, "MaxFileDescriptorCount")) {

                ri.setOpenFileDescriptorCount(
                    JmxTools.getLongAttr(mbeanServer, osOName, "OpenFileDescriptorCount"));
                ri.setMaxFileDescriptorCount(
                    JmxTools.getLongAttr(mbeanServer, osOName, "MaxFileDescriptorCount"));
            }

            return ri;
        } catch (Exception e) {
            logger.debug("OS information is unavailable");
            logger.trace("", e);
            return null;
        }
    }
}
