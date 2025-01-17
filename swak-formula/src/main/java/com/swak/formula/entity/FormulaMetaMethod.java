package com.swak.formula.entity;

import com.swak.common.dto.base.DTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * FormulaMetaMethod.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
@Data
@Accessors(chain = true)
public class FormulaMetaMethod implements DTO {
    private String name;
    private String description;
    private List<ParamMetaType> paramMetaTypes;
    private Class<?> returnType;

    public boolean isStrReturnType(){
        return String.class.isAssignableFrom(returnType);
    }

    @Data
    @Accessors(chain = true)
    public static class ParamMetaType implements DTO {
        private String name;
        private Class<?> parameterType;
        private String paramName;
    }

    public static void main(String[] args) {
        Class<?> returnType = Date.class;
        System.out.println(String.class.isAssignableFrom(returnType));
    }
}
