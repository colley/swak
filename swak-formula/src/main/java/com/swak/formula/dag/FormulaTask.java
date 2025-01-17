package com.swak.formula.dag;

import com.swak.formula.ExpressionRunner;
import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

/**
 * 公式任务
 *
 * @author colley
 * @since 1.0
 */
public class FormulaTask implements Runnable {

    private final ExpressionRunner expressionRunner;

    private final Binding binding;

    private final FormulaData formulaData;

    /**
     * Instantiates a new Function task.
     *
     * @param expressionRunner the function engine
     * @param binding          the binding
     * @param formulaData      the formula data
     */
    public FormulaTask(ExpressionRunner expressionRunner, Binding binding, FormulaData formulaData) {
        this.expressionRunner = expressionRunner;
        this.binding = binding;
        this.formulaData = formulaData;
    }

    @Override
    public void run() {
        Object result = formulaData.getValue();
        // 初始值先放入上下文，防止自引用问题
        if (result != null) {
            // 明细表索引
            if (formulaData.getIndex() > -1) {
                binding.setVariable(formulaData.getName() + "[" + formulaData.getIndex() + "]", result);
            } else {
                binding.setVariable(formulaData.getName(), result);
            }
        }
        if (StringUtils.isNotEmpty(formulaData.getFormula())) {
            result = expressionRunner.run(formulaData.getFormula(), binding);
        }
        // 计算出的结果放入上下文
        if (formulaData.getIndex() > -1) {
            Object[] values;
            try {
                values = (Object[]) binding.getVariable(formulaData.getName());
            } catch (MissingPropertyException e) {
                values = newInstance(result, formulaData.getIndex() + 1);
            }
            if (formulaData.getIndex() >= values.length) {
                values = Arrays.copyOf(values, formulaData.getIndex() + 1);
            }
            values[formulaData.getIndex()] = result;
            binding.setVariable(formulaData.getName(), values);
            // 明细表索引
            binding.setVariable(formulaData.getName() + "[" + formulaData.getIndex() + "]", result);
        } else {
            binding.setVariable(formulaData.getName(), result);
        }
        formulaData.setValue(result);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return formulaData.getName();
    }

    /**
     * Gets index.
     *
     * @return the index
     */
    public int getIndex() {
        return formulaData.getIndex();
    }

    /**
     * Gets formula.
     *
     * @return the formula
     */
    public String getFormula() {
        return formulaData.getFormula();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FormulaTask that = (FormulaTask) o;
        return formulaData.equals(that.formulaData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formulaData);
    }

    private Object[] newInstance(Object result, int newLength) {
        if (result instanceof Number) {
            return (Object[]) Array.newInstance(result.getClass(), newLength);
        }
        return (Object[]) Array.newInstance(result.getClass().getComponentType(), newLength);
    }
}
