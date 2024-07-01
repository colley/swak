package com.swak.demo.enums;


import com.swak.common.enums.EnumType;

/**
 * @author colley
 */
public enum SceneSymbolType implements EnumType {
    EQ("=", "="),
    NE("≠", "≠"),
    LIKE("LIKE", "包含通配符"),
    NOT_LIKE("NOT_LIKE", "不含通配符"),
    REG_EX("REG_EX", "正则表达式"),
    IS_NULL("IS_NULL", "为空"),
    IS_NOT_NULL("NOT_NULL", "不为空"),
    ;

    private String type;
    private String name;

    SceneSymbolType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static SceneSymbolType of(String symbol) {
        for (SceneSymbolType symbolEnum : values()) {
            if (symbolEnum.getValue().equals(symbol)) {
                return symbolEnum;
            }
        }
        throw new IllegalArgumentException();
    }

    public static boolean not(SceneSymbolType symbolType) {
        return symbolType.equals(NE) || symbolType.equals(NOT_LIKE);
    }

    public static SceneSymbolType map(String symbol) {
        for (SceneSymbolType e : values()) {
            if (e.getValue().equals(symbol)) {
                return e;
            }
        }
        throw new IllegalStateException();
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
