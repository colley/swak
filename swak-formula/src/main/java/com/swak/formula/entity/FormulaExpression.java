package com.swak.formula.entity;

import groovy.lang.Script;
import lombok.Data;

@Data
public class FormulaExpression extends CompileExpression {
    public static FormulaExpression EMPTY = new FormulaExpression();

    private Class<Script> compileClass;

    public void  apply(CompileExpression formulaPre) {
        super.setOriginalScript(formulaPre.getOriginalScript());
        super.setCompileScript(formulaPre.getCompileScript());
        super.setInputVariables(formulaPre.getInputVariables());
        super.setVariables(formulaPre.getVariables());
        super.setRelatedFunctions(formulaPre.getRelatedFunctions());
    }
}
