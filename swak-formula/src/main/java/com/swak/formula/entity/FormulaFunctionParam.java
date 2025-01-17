package com.swak.formula.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FormulaFunctionParam implements java.io.Serializable{
    private String functionName;
    private List<String> functionParams;
}
