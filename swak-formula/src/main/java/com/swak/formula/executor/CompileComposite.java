package com.swak.formula.executor;

import com.swak.formula.ExpressionRunner;
import com.swak.formula.entity.CompileExpression;
import com.swak.formula.entity.FormulaAroundCompile;
import com.swak.formula.entity.FormulaExpression;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.spi.FormulaCompile;
import com.swak.formula.spi.FormulaCompileAspect;

import java.util.Map;
import java.util.Objects;

/**
 * CompileComposite.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class CompileComposite {

    protected final ExpressionRunner expressionRunner;

    public CompileComposite(ExpressionRunner expressionRunner) {
        this.expressionRunner = expressionRunner;
    }

    public FormulaExpression compile(String logic, Map<String, String> mockData) throws FormulaCompileException {
        FormulaAroundCompile formulaAround = new FormulaAroundCompile().setOriginalScript(logic).setMockData(mockData);
        try {
            FormulaCompileAspect.getAroundAspect().beforeCompile(formulaAround);
            CompileExpression compileExpression = FormulaCompile.getCompile().compile(logic, mockData);
            if (Objects.nonNull(compileExpression)) {
                formulaAround.setCompileScript(compileExpression.getCompileScript());
                FormulaExpression formulaRunner = expressionRunner.parseClass(compileExpression.getCompileScript());
                formulaRunner.apply(compileExpression);
                return formulaRunner;
            }
            return FormulaExpression.EMPTY;
        } catch (Exception e) {
            FormulaCompileAspect.getAroundAspect().onError(formulaAround, e);
            throw e;
        } finally {
            FormulaCompileAspect.getAroundAspect().afterCompile(formulaAround);
        }
    }

    public boolean matchFunction(String functionName) {
        return FormulaCompile.getCompile().matchFunction(functionName);
    }
}
