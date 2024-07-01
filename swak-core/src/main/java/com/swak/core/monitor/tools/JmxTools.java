
package com.swak.core.monitor.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.openmbean.CompositeData;

/**
 * jmx 工具类
 * 
 * @author liuliang8
 *
 */
public final class JmxTools {

    private static final Logger logger = LoggerFactory.getLogger(JmxTools.class);

    private JmxTools() {

    }

    public static String[] getArrayAttr(MBeanServer mbeanServer, ObjectName objName,
        String attrName) throws Exception {

        Object obj = getAttribute(mbeanServer, objName, attrName);
        if (obj != null && obj instanceof String[]) {
            return (String[]) obj;
        }
        return null;
    }

    public static Object getAttribute(MBeanServer mbeanServer, ObjectName objName, String attrName)
        throws Exception {

        try {
            return mbeanServer.getAttribute(objName, attrName);
        } catch (AttributeNotFoundException e) {
            logger.error("{} does not have '{}' attribute", objName, attrName);
            logger.trace("", e);
            return null;
        }
    }

    public static long getLongAttr(MBeanServer mbeanServer, ObjectName objName, String attrName,
        long defaultValue) {

        try {
            Object obj = mbeanServer.getAttribute(objName, attrName);
            return obj == null ? defaultValue : ((Long) obj);
        } catch (Exception e) {
            logger.trace("", e);
            return defaultValue;
        }
    }

    public static long getLongAttr(CompositeData cds, String name) {
        Object obj = cds.get(name);
        if (obj != null && obj instanceof Long) {
            return (Long) obj;
        }
        return 0;
    }

    public static long getLongAttr(MBeanServer mbeanServer, ObjectName objName, String attrName)
        throws Exception {

        return (Long) mbeanServer.getAttribute(objName, attrName);
    }

    public static int getIntAttr(MBeanServer mbeanServer, ObjectName objName, String attrName)
        throws Exception {

        return (Integer) mbeanServer.getAttribute(objName, attrName);
    }

    public static int getIntAttr(CompositeData cds, String name, int defaultValue) {
        Object obj = cds.get(name);

        if (obj != null && obj instanceof Integer) {
            return (Integer) obj;
        }
        return defaultValue;
    }

    public static String getStringAttr(MBeanServer mbeanServer, ObjectName objName, String attrName)
        throws Exception {

        Object obj = getAttribute(mbeanServer, objName, attrName);
        return obj == null ? null : obj.toString();
    }

    public static String getStringAttr(CompositeData cds, String name) {
        Object obj = cds.get(name);
        return obj != null ? obj.toString() : null;
    }

    public static boolean getBooleanAttr(CompositeData cds, String name) {
        Object obj = cds.get(name);
        return obj != null && obj instanceof Boolean && ((Boolean) obj);
    }

    public static boolean hasAttribute(MBeanServer server, ObjectName mbean, String attrName)
        throws Exception {

        MBeanInfo info = server.getMBeanInfo(mbean);
        MBeanAttributeInfo[] ai = info.getAttributes();
        for (MBeanAttributeInfo attribInfo : ai) {
            if (attribInfo.getName().equals(attrName)) {
                return true;
            }
        }
        return false;
    }

}
