package com.swak.formula.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author: yuanchao.ma
 * @Date: 2022/9/7 17:47
 */
@Data
@Accessors(chain = true)
public class FormulaFunctionVo implements java.io.Serializable {

    private String functionId;
    private String groupName;
    private String name;
    private String label;
    private String description;
    private String value;
    private List<FormulaParameterTypeVo> parameterTypes;
    private String returnType;
    private Integer  returnOfIndex;
    private String  returnOfType;

    @Data
    @Accessors(chain = true)
    public static class FormulaParameterTypeVo implements java.io.Serializable {
        private String type;
        private String paramName;
        private String label;
        private String viewType;
        private String defaultValue;
        private  Boolean isVariable;
    }
}
