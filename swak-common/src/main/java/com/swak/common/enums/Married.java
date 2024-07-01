package com.swak.common.enums;

import java.util.Objects;

public enum Married {
    YES(1),
    NO(0);

    private Integer value;

    private Married(Integer value) {
        this.value = value;
    }

    public static boolean isTrue(Integer value) {
        return Objects.equals(YES.value, value);
    }

    public static boolean isTrue(Boolean value) {
        if (Objects.isNull(value)) {
            return false;
        }
        return value.booleanValue();
    }

    public static Integer convert(Boolean value) {
        if (value == null) {
            return Married.NO.getValue();
        }
        return value ? Married.YES.getValue() : Married.NO.getValue();
    }

    public Integer getValue() {
        return value;
    }
}
