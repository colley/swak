package com.swak.autoconfigure.condition;


import com.swak.common.enums.EnumType;

import java.util.Objects;

/**
 * @author colley
 */
public enum ConditionalSymbol {
    EQ("="),
    NE("≠"),
    IS_EMPTY("IS_EMPTY"),
    IS_NOT_EMPTY("NOT_EMPTY"),
    ;

    private final String type;

    ConditionalSymbol(String type) {
        this.type = type;
    }

    public static ConditionalSymbol of(String symbol) {
        for (ConditionalSymbol symbolEnum : values()) {
            if (symbolEnum.getValue().equals(symbol)) {
                return symbolEnum;
            }
        }
        return null;
    }

    public static boolean isEmpty(ConditionalSymbol symbol) {
       return Objects.equals(ConditionalSymbol.IS_EMPTY,symbol);
    }


    public static ConditionalSymbol map(String symbol) {
        for (ConditionalSymbol e : values()) {
            if (e.getValue().equals(symbol)) {
                return e;
            }
        }
        return null;
    }

    public String getValue() {
        return this.type;
    }

}
