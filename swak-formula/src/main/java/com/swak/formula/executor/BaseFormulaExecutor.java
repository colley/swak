package com.swak.formula.executor;

import com.swak.formula.ExpressionRunner;
import com.swak.formula.common.Constant;
import com.swak.formula.entity.FormulaAround;
import com.swak.formula.entity.FormulaExpression;
import com.swak.formula.entity.FormulaVariable;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.exception.FormulaRuntimeException;
import com.swak.formula.spi.FormulaRunAspect;
import groovy.lang.Binding;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.Map;
import java.util.Objects;

/**
 * BaseFormulaExecutor.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public abstract class BaseFormulaExecutor implements FormulaExecutor {

    protected ExpressionRunner expressionRunner;

    protected final ConversionService conversionService;

    protected CompileComposite compileComposite;

    protected final FormulaRunAspect formulaRunAspect;

    public BaseFormulaExecutor() {
        this.conversionService = DefaultConversionService.getSharedInstance();
        this.formulaRunAspect = FormulaRunAspect.getFormulaRunAspect();
    }

    public BaseFormulaExecutor(ExpressionRunner expressionRunner) {
        this();
        this.apply(expressionRunner);
    }

    protected void apply(ExpressionRunner expressionRunner) {
        this.expressionRunner = expressionRunner;
        this.compileComposite = new CompileComposite(expressionRunner);
    }

    @Override
    public FormulaExpression compile(String logicFormula, Map<String, String> mockData) throws FormulaCompileException {
        if (Objects.isNull(logicFormula)) {
            return FormulaExpression.EMPTY;
        }
        return compileComposite.compile(logicFormula, mockData);
    }

    /**
     * Execute the FormulaExpression and return the result of execution
     */
    @Override
    public <T> T execute(FormulaExpression formulaExpression, FormulaVariable variables, Class<T> clazz) throws FormulaRuntimeException {
        Binding binding = new Binding(variables.getVariables());
        if (MapUtils.isNotEmpty(formulaExpression.getVariables())) {
            formulaExpression.getVariables().forEach(binding::setVariable);
        }
        binding.setVariable(Constant.STATE_CONTEXT, variables.getStateContext());
        FormulaAround formulaAround = new FormulaAround().setBinding(binding).setScript(formulaExpression.getCompileScript());
        formulaRunAspect.beforeProcess(variables,formulaAround);
        try {
            Object result = expressionRunner.run(formulaExpression, binding);
            T convertResult = this.convert(result, binding, variables, clazz);
            formulaRunAspect.onSuccess(variables,formulaAround.setResult(convertResult));
            return convertResult;
        } catch (Exception e) {
            formulaRunAspect.onError(variables,formulaAround, e);
            if (causedByNullPointerException(e)) {
                return null;
            }
            throw e;
        } finally {
            formulaRunAspect.afterProcess(variables,formulaAround);
        }
    }

    private boolean causedByNullPointerException(Throwable e) {
        while (e != null) {
            if (e instanceof NullPointerException) {
                return true;
            } else {
                e = e.getCause();
            }
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    private <T> T convert(Object result, Binding binding, FormulaVariable variables, Class<T> clazz) {
        variables.binding(binding.getVariables());
        if (binding.hasVariable(Constant.CURRENT_VAR_NAME)) {
            result = binding.getVariable(Constant.CURRENT_VAR_NAME);
        }
        if (Objects.isNull(result)) {
            return null;
        }
        if (Objects.isNull(clazz)) {
            return (T) result;
        }
        try {
            return conversionService.convert(result, clazz);
        } catch (Exception e) {
            throw new FormulaRuntimeException("[Swak-Formula] result not convert to " + clazz.getName() + "", e);
        }
    }

    @Override
    public <T> T execute(String scriptText, FormulaVariable variables, Class<T> clazz) throws FormulaCompileException, FormulaRuntimeException {
        FormulaExpression formulaExpression = expressionRunner.parseClass(scriptText);
        formulaExpression.setOriginalScript(scriptText);
        return execute(formulaExpression, variables, clazz);
    }

    @Override
    public void close() throws Exception {
        log.info("CLose {} ", this.getClass().getName());
        if (Objects.nonNull(expressionRunner)) {
            expressionRunner.close();
        }
    }

}
