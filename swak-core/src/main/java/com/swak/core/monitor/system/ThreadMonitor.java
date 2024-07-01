package com.swak.core.monitor.system;

import com.swak.core.monitor.model.SunThread;
import com.swak.core.monitor.model.ThreadStackElement;
import com.swak.core.monitor.tools.JmxTools;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;


/**
 * 线程运行状态监控
 * 
 * @author liuliang8
 *
 */

public class ThreadMonitor {



    /**
     * 获取线程运行信息
     * 
     * @return
     * @throws Exception
     */
    public static List<SunThread> geThreads() throws Exception {
        List<SunThread> threads = null;
        int executionStackDepth = 1;

        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName threadingOName = new ObjectName("java.lang:type=Threading");

        long[] deadlockedIds =
            (long[]) mbeanServer.invoke(threadingOName, "findMonitorDeadlockedThreads", null, null);
        long[] allIds = (long[]) mbeanServer.getAttribute(threadingOName, "AllThreadIds");

        if (allIds != null) {
            threads = new ArrayList<>(allIds.length);

            for (long id : allIds) {
                CompositeData cd =
                    (CompositeData) mbeanServer.invoke(threadingOName, "getThreadInfo",
                        new Object[] {id, executionStackDepth}, new String[] {"long", "int"});

                if (cd != null) {
                    SunThread st = new SunThread();
                    st.setId(JmxTools.getLongAttr(cd, "threadId"));
                    st.setName(JmxTools.getStringAttr(cd, "threadName"));
                    st.setState(JmxTools.getStringAttr(cd, "threadState"));
                    st.setSuspended(JmxTools.getBooleanAttr(cd, "suspended"));
                    st.setInNative(JmxTools.getBooleanAttr(cd, "inNative"));
                    st.setLockName(JmxTools.getStringAttr(cd, "lockName"));
                    st.setLockOwnerName(JmxTools.getStringAttr(cd, "lockOwnerName"));
                    st.setWaitedCount(JmxTools.getLongAttr(cd, "waitedCount"));
                    st.setBlockedCount(JmxTools.getLongAttr(cd, "blockedCount"));
                    st.setDeadlocked(contains(deadlockedIds, st.getId()));

                    CompositeData[] stack = (CompositeData[]) cd.get("stackTrace");
                    if (stack.length > 0) {
                        CompositeData cd2 = stack[0];
                        ThreadStackElement tse = new ThreadStackElement();
                        tse.setClassName(JmxTools.getStringAttr(cd2, "className"));
                        tse.setFileName(JmxTools.getStringAttr(cd2, "fileName"));
                        tse.setMethodName(JmxTools.getStringAttr(cd2, "methodName"));
                        tse.setLineNumber(JmxTools.getIntAttr(cd2, "lineNumber", -1));
                        tse.setNativeMethod(JmxTools.getBooleanAttr(cd2, "nativeMethod"));
                        st.setExecutionPoint(tse);
                    }

                    threads.add(st);
                }
            }
        }
        return threads;
    }


    private static boolean contains(long[] haystack, long needle) {
        if (haystack != null) {
            for (long hay : haystack) {
                if (hay == needle) {
                    return true;
                }
            }
        }
        return false;
    }
}
