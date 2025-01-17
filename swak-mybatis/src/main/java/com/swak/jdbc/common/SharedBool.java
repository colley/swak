package com.swak.jdbc.common;

import java.util.Optional;


public class SharedBool extends SharedValue<Boolean> {

    public SharedBool() {
        super();
    }

    public SharedBool(boolean value) {
        super(value);
    }

    /**
     * SharedString 里是 ""
     */
    public static SharedBool emptyFalse() {
        return new SharedBool(false);
    }

    public void toEmpty() {
        toNull();
    }

    public boolean isTrue() {
        return Optional.ofNullable(value).orElse(false);
    }


    public void toNull() {
        value = null;
    }
}