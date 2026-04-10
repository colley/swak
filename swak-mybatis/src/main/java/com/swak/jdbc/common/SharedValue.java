package com.swak.jdbc.common;


import java.io.Serializable;
import java.util.Objects;

public class SharedValue<T> implements Serializable {
    /**
     * 共享
     */
    protected T value;

    public SharedValue(){}

    public SharedValue(T value){
        this.value = value;
    }

    public void toEmpty() {
        toNull();
    }

    @Override
    public String toString() {
        return Objects.nonNull(value)?value.toString():null;
    }

    public T getValue(){
        return this.value;
    }

    public void setValue(T value){
        this.value = value;
    }

    public void toNull() {
        value = null;
    }
}