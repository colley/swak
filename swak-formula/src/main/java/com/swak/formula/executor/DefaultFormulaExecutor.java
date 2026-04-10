package com.swak.formula.executor;


import com.swak.formula.ExpressionRunnerImpl;
import com.swak.formula.entity.VariableContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 加载通用函数库 FunctionScript.loadAllFunctionMapper 自定义函数可以通过SPI配置
 * META-INF/services/com.swak.formula.spi.FormulaPlugin
 *
 * @author yuanchao.ma
 * @date 2022/9/23 17:03
 */

@Slf4j
public class DefaultFormulaExecutor extends BaseFormulaExecutor {
    public DefaultFormulaExecutor() {
        super(new ExpressionRunnerImpl());
    }


    public static void main(String[] args) {
        String input = "abc=10;println(abc);abc=abc+10;";
        FormulaExecutor.getInstance().execute(input, new VariableContext());
    }
}
