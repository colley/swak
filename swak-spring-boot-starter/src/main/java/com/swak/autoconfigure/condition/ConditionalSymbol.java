package com.swak.autoconfigure.condition;


import com.swak.common.enums.EnumType;

import java.util.Objects;

/**
 * @author colley
 */
public enum ConditionalSymbol implements EnumType {
    EQ("=", "="),
    NE("≠", "≠"),
    IS_EMPTY("IS_EMPTY", "为空"),
    IS_NOT_EMPTY("NOT_EMPTY", "不为空"),
    ;

    private final String type;
    private final  String name;

    ConditionalSymbol(String type, String name) {
        this.type = type;
        this.name = name;
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

    @Override
    public String getValue() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
