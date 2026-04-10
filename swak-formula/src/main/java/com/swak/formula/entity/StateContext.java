package com.swak.formula.entity;

import org.apache.commons.collections4.MapUtils;

import java.util.*;

public class StateContext extends LinkedHashMap<Object, Object> {
    private static final long serialVersionUID = -1462504935270338298L;

    public StateContext() {
    }

    public StateContext(Map<?, ?> result) {
        this.putAll(result);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public StateContext put(Object key, Object value) {
        super.put(key, value);
        return this;
    }

    public String getString(String key, String defaultValue) {
        Object val = get(key);
        if (val != null && val instanceof Boolean) {
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

    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, ?> getLinkedHashMap(String key) {
        return (LinkedHashMap<String, ?>) MapUtils.getObject(this, key);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMapObject(String key) {
        return (Map<String, Object>) MapUtils.getMap(this, key);
    }

    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return MapUtils.getInteger(this, key, defaultValue);
    }

    public Long getLong(String key, Long defaultValue) {
        return MapUtils.getLong(this, key, defaultValue);
    }

    public Long getLong(String key) {
        return getLong(key, null);
    }

    public Double getDouble(String key) {
        return getDouble(key, null);
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
