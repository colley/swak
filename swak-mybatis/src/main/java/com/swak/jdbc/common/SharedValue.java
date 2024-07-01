package com.swak.jdbc.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
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

    public void toNull() {
        value = null;
    }
}