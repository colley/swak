package com.swak.license.spi.config;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.MapUtils;

import java.util.*;

/**
 * LicenseCheckExtra.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LicenseCheckExtra extends LinkedHashMap<String, Object> {

    public LicenseCheckExtra() {}

    public LicenseCheckExtra(Map<String, ?> result) {
        this.putAll(result);
    }

    public LicenseCheckExtra(JSONObject jsonObject) {
        this.putAll(jsonObject);
    }

    public  List<String> getIpAddress() {
         return (List<String>) getList("ipAddress");
    }

    public  List<String> getMacAddress() {
        return (List<String>) getList("macAddress");
    }

    public String getString(String key) {
        return getString(key, "");
    }


    @Override
    public LicenseCheckExtra put(String key, Object value) {
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
