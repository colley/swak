package com.swak.common.enums;

import java.util.Objects;

public enum Status {
    VALID(0),
    INVALID(-1);

    private Integer value;

    private Status(Integer value) {
        this.value = value;
    }

    public static boolean isValid(Integer value) {
        return Objects.equals(VALID.value, value);
    }

    public Integer getValue() {
        return value;
    }
}
