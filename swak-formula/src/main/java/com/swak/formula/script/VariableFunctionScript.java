package com.swak.formula.script;

import com.swak.formula.DagExpressionRunner;
import com.swak.formula.common.Constant;
import com.swak.formula.dag.FormulaTask;
import com.swak.formula.exception.FormulaRuntimeException;
import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import groovy.transform.CompileStatic;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * VariableFunctionScript.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
@CompileStatic
public class VariableFunctionScript extends FunctionScript {

    private static final String VARIABLE_START = "#{";
    private static final String VARIABLE_END = "}";

    @Override
    public Object invokeMethod(String name, Object args) {
        // 解析变量
        args = parseVariables((Object[]) args);
        // 方法调用
        return super.invokeMethod(name, args);
    }

    private Object[] parseVariables(Object[] args) {
        args =  ArrayUtils.addFirst(args,VARIABLE_START+ Constant.STATE_CONTEXT+VARIABLE_END);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String) {
                String argStr = (String) arg;
                if (argStr.startsWith(VARIABLE_START) && argStr.endsWith(VARIABLE_END)) {
                    String variable = argStr.substring(VARIABLE_START.length(), argStr.length() - 1);

                    // 对于明细引用明细，修正为当前行
                    variable = parseVariable(variable, getBinding());

                    // 从绑定上下文中取值
                    Object value = null;
                    try {
                        value = getBinding().getVariable(variable);
                    } catch (MissingPropertyException e) {
                        // ignore
                    }
                    args[i] = value;
                }
            }
        }
        return args;
    }

    @SuppressWarnings("unchecked")
    private String parseVariable(String variable, Binding binding) {
        try {
            int index = (int) binding.getVariable(DagExpressionRunner.BINDING_CURRENT_INDEX);
            if (index > -1) {
                List<FormulaTask> tasks = (List<FormulaTask>) binding.getVariable(DagExpressionRunner.BINDING_TASKS);
                FormulaTask task = tasks.stream()
                        .filter(e -> e.getName().equals(variable))
                        .findFirst()
                        .orElseThrow(() -> new FormulaRuntimeException("[Swak-Formula] formula Dag failed，not found _tasks variable"));
                if (task.getIndex() > -1) {
                    return variable + "[" + index + "]";
                }
            }
            return variable;
        } catch (MissingPropertyException e) {
            return variable;
        }
    }

}
