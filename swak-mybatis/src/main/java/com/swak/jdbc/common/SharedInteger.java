package com.swak.jdbc.common;

public class SharedInteger extends SharedValue<Integer> {

    public SharedInteger() {
        super();
    }

    public SharedInteger(Integer value) {
        super(value);
    }

    public void incr() {
        value = value + 1;
    }

    public void toEmpty() {
        toNull();
    }

    public void toNull() {
        value = null;
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}