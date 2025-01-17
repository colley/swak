package com.swak.formula.executor;


import com.google.common.collect.Maps;
import com.swak.formula.entity.FormulaExpression;
import com.swak.formula.entity.FormulaVariable;
import com.swak.formula.exception.FormulaCompileException;
import com.swak.formula.exception.FormulaRuntimeException;

import java.util.Map;

public interface FormulaExecutor extends  FormulaAutoCloseable {

    /**
     * 编译脚本
     */
    default FormulaExpression compile(String logicFormula) throws FormulaCompileException {
        return  compile(logicFormula, Maps.newHashMap());
    }

     FormulaExpression compile(String logicFormula, Map<String,String> mockData) throws FormulaCompileException;

    /**
     * 脚本执行
     */
     <T> T execute(FormulaExpression fdcExpression, FormulaVariable variables, Class<T> clazz) throws FormulaRuntimeException;

    /**
     * Execute the FormulaExpression and return the result of execution
     */
    default   <T> T  execute(FormulaExpression fdcExpression, FormulaVariable variables) throws FormulaRuntimeException{
        return  execute(fdcExpression,variables,null);
    }

    /**
     * 脚本执行
     */
   default  <T> T execute(String scriptText, FormulaVariable variables) throws FormulaCompileException,FormulaRuntimeException{
        return execute(scriptText,variables,null);
    }

    /**
     * 脚本执行
     */
     <T> T execute(String scriptText, FormulaVariable variables,Class<T> clazz) throws FormulaCompileException,FormulaRuntimeException;

    /**
     * 获取实例
     */
      static FormulaExecutor getInstance() {
        return FormulaExecutorInstance.INSTANCE;
    }

     class FormulaExecutorInstance {
        private static final FormulaExecutor INSTANCE = new DefaultFormulaExecutor();
    }

}
