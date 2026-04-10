package com.swak.common.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruLinkedHashMap<K,V> extends LinkedHashMap<K,V> {
    private int capacity;

    public LruLinkedHashMap() {
        super();
    }
    public LruLinkedHashMap(int capacity) {
        this(capacity,false);
    }

    public LruLinkedHashMap(int capacity, boolean accessOrder) {
        super(capacity, 0.75F, accessOrder);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        if(capacity<=0){
            return false;
        }
        return size() > capacity;
    }
}