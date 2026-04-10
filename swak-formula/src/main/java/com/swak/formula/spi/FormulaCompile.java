package com.swak.formula.spi;


import com.google.common.collect.Maps;
import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SpiServiceFactory;
import com.swak.formula.entity.CompileExpression;
import com.swak.formula.entity.FormulaFunctionParam;
import com.swak.formula.exception.FormulaCompileException;

import java.util.List;
import java.util.Map;

;

public interface FormulaCompile extends SpiPriority {

    default CompileExpression compile(String logic) throws FormulaCompileException {
        return compile(logic, Maps.newHashMap());
    }

    CompileExpression compile(String logic, Map<String, String> mockData) throws FormulaCompileException;

    default String formatVariables(String formulaStr, Map<String, String> formulaValue, List<FormulaFunctionParam> relatedFunctions) {
        return formulaStr;
    }

    default String formatVariables(String formulaStr, List<FormulaFunctionParam> relatedFunctions) {
        return formatVariables(formulaStr, Maps.newHashMap(), relatedFunctions);
    }

    static FormulaCompile getCompile() {
        return SpiServiceFactory.loadFirst(FormulaCompile.class);
    }

    /**
     * 判断是不是自定函数
     */
    default boolean matchFunction(String functionName){
        return (Character.isUpperCase(functionName.charAt(0)));
    }
}
