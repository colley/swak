/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 20-3-23 上午9:28
 * History:
 */
package com.swak.core.support;

import com.swak.common.util.GetterUtil;

import java.text.MessageFormat;
import java.util.Properties;

/**
 * PropertieConfig.java
 *
 * @author ColleyMa
 * @version 20-3-23 上午9:28
 */
public final class PropertiesFactory {

    protected static Properties localProperties = new Properties();

    public static String getString(String key) {
        return localProperties.getProperty(key);
    }

    public static String getFomatString(String key, Object... args) {
        String message = localProperties.getProperty(key);

        if ((args == null) || (args.length == 0)) {
            return message;
        }

        return MessageFormat.format(message, args);
    }

    public static String getString(String key, String defaultValue) {
        return localProperties.getProperty(key, defaultValue);
    }

    public static Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static Boolean getBoolean(String key, boolean defaultValue) {
        String propVal = getString(key);
        return GetterUtil.getBoolean(propVal, defaultValue);
    }

    public static Integer getInt(String key) {
        return getInt(key, null);
    }

    public static Integer getInt(String key, Integer defaultValue) {
        String propVal = getString(key);
        return GetterUtil.getInteger(propVal, defaultValue);
    }

    public static Long getLong(String key) {
        return getLong(key, null);
    }

    public static Long getLong(String key, Long defaultValue) {
        String propVal = getString(key);
        return GetterUtil.getLong(propVal, defaultValue);
    }

}
