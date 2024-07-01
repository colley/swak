package com.swak.common.convert;

import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * FormatRegisterFactory.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class FormatRegisterFactory {

    private static final Map<Class<?>, Object> FORMAT_REGISTER = Maps.newConcurrentMap();


    public static void registerDateFormat(String format) {
        registerFormat(Date.class, format);
    }

    public static void registerFormat(Class<?> formatType, Object dateFormat) {
        if (Objects.nonNull(formatType) && Objects.nonNull(dateFormat)) {
            FORMAT_REGISTER.put(formatType, dateFormat);
        }
    }

    public static <T> T getFormat(Class<?> formatType) {
        if (Objects.nonNull(formatType)) {
            return (T) FORMAT_REGISTER.get(formatType);
        }
        return null;
    }

    public static String getDateFormat() {
        return getFormat(Date.class);
    }

}
