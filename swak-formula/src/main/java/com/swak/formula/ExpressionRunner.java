package com.swak.formula;


import com.swak.formula.entity.FormulaExpression;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.executor.FormulaAutoCloseable;
import groovy.lang.Binding;

/**
 * ExpressionRunner.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public interface ExpressionRunner extends FormulaAutoCloseable {

    /**
     * 预编译
     */
    FormulaExpression parseClass(String expression) throws FormulaCompileException;

    /**
     * Run scriptText.
     */
     <T> T run(String scriptText);

    /**
     * Run object.
     */
     <T> T run(String scriptText, Binding binding);


    /**
     *  执行编译过的脚本
     */
     <T> T run(FormulaExpression formulaExpression, Binding binding);
}
