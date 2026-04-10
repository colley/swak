package com.swak.core.monitor.tools;

import javax.management.openmbean.CompositeData;

public class ByteToM {

    /**
     * 把字节转换成M
     * 
     * @param cd
     * @param name
     */
    public static String convert(CompositeData cd, String name) {
        long size = JmxTools.getLongAttr(cd, name);
        return convert(size);
    }

    public static String convert(long size) {
        return new StringBuilder().append(size / (1024 * 1024)).append("M").toString();
    }

}
