package com.swak.formula.script;


import com.swak.formula.exception.FormulaRuntimeException;
import com.swak.formula.reflect.FormulaPluginRegistry;
import com.swak.formula.reflect.MethodInvocation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * FunctionScript.java
 * 函数脚本
 *
 * @author colley.ma
 * @since 2.4.0
 **/
@Slf4j
public class FunctionScript extends BaseScript {

    @Override
    public Object invokeMethod(String name, Object args) {
        MethodInvocation invocation = FormulaPluginRegistry.getInstance().getMethodInvocation(name);
        if (invocation == null) {
            throw new FormulaRuntimeException("[Swak-Formula] can not found method for function: " + name);
        }
        try {
            if (invocation.hasContextParam()) {
                return invocation.invoke((Object[]) args);
            } else {
                //不存在上下文，去掉第一个参数(第一参数是上下文）
                Object[] newArgs = (Object[]) args;
                return invocation.invoke(ArrayUtils.remove(newArgs, 0));
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new FormulaRuntimeException("[Swak-Formula] invoke method failed", e);
        }
    }
}