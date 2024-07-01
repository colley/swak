package com.swak.common.dto;

/**
 * CarrierValue
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/11 15:16
 **/
public class CarrierValue<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
