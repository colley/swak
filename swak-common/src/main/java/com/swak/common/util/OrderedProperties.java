

package com.swak.common.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Properties;


public class OrderedProperties extends Properties {
    private final LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();

    @Override
    public Object put(Object key, Object value) {
        return linkedHashMap.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return linkedHashMap.get(key);
    }

    @Override
    public Object remove(Object key) {
        return linkedHashMap.remove(key);
    }

    // Override other methods as needed

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(linkedHashMap.keySet());
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return Collections.enumeration(linkedHashMap.values());
    }
}
