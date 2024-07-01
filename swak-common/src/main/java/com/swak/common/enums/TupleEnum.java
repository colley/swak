package com.swak.common.enums;

import java.util.Objects;

public enum TupleEnum {
    NO(0),
    YES(1),
    TUPLE2(2),
    TUPLE3(3),
    TUPLE4(4),
    TUPLE5(5),
    TUPLE6(6),
    TUPLE7(7),
    TUPLE8(8),
    TUPLE9(9),
    TUPLE10(10),
    TUPLE11(11),
    TUPLE12(12),
    TUPLE13(13),
    TUPLE14(14),
    TUPLE15(15),
    TUPLE16(16),
    ;
    /**
     * 编码
     */
    private Integer value;

    TupleEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static boolean yes(Integer value) {
        return Objects.equals(YES.value, value);
    }
}
