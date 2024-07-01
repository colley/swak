package com.swak.common.util;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * ClassName: MapBean.java
 * map转换
 * @author colley.ma
 * @since 2021年3月15日
 */
public class MapBean extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = -1462504935270338298L;

    public MapBean() {}

    public MapBean(Map<String, ?> result) {
        this.putAll(result);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getStringIgnoreCase(String key) {
        return StringUtils.firstNonBlank(getString(key), getString(key.toLowerCase()),
                getString(key.toUpperCase()), getString(matchReplace(key)),
                getString(matchReplace(key).toLowerCase()), getString(matchReplace(key).toUpperCase()));
    }

    private String matchReplace(String filedName) {
        if (StringUtils.isEmpty(filedName)) {
            return "";
        }
        return RegExUtils.replaceAll(filedName, " ", "_").replaceAll("-", "_").trim();
    }

    @Override
    public MapBean put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public String getString(String key, String defaultValue) {
        Object val = get(key);
        if (val instanceof Boolean) {
            return val.toString().toUpperCase();
        }
        return MapUtils.getString(this, key, defaultValue);
    }

    public Boolean getBoolean(String key, boolean defaultValue) {
        return MapUtils.getBooleanValue(this, key, defaultValue);
    }

    public Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> getMap(String key) {
        return (Map<String, ?>) MapUtils.getMap(this, key);
    }

    public Integer getInteger(String key) {
        return getInteger(key, 0);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return MapUtils.getInteger(this, key, defaultValue);
    }

    public Long getLong(String key, Long defaultValue) {
        return MapUtils.getLong(this, key, defaultValue);
    }

    public Long getLong(String key) {
        return getLong(key, 0L);
    }

    public Double getDouble(String key) {
        return getDouble(key, 0d);
    }

    public Object getObject(String key) {
        return MapUtils.getObject(this, key);
    }

    public Collection<?> getCollection(String key) {
        Object obj = getObject(key);
        if (obj == null) {
            return null;
        }
        if (obj instanceof Collection<?>) {
            return (Collection<?>) obj;
        }
        return null;
    }

    public List<?> getList(String key) {
        Collection<?> collection = getCollection(key);
        if (collection == null) {
            return null;
        }
        if (collection instanceof List<?>) {
            return (List<?>) collection;
        }
        return new ArrayList<>(collection);
    }

    public Double getDouble(String key, Double defaultValue) {
        return MapUtils.getDouble(this, key, defaultValue);
    }
}
