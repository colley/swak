package com.swak.test.formula;

import com.swak.formula.ExpressionRunner;
import com.swak.formula.ExpressionRunnerImpl;
import com.swak.formula.exception.FormulaRuntimeException;
import com.swak.formula.plugin.MathFunctionPlugin;
import com.swak.formula.reflect.FormulaPluginRegistry;
import groovy.lang.Binding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * swak-formula 冒烟测试：验证 Groovy 5（org.apache.groovy:groovy 5.0.8）下
 * 公式的编译、插件函数调用、变量绑定链路仍然可用。
 */
class FormulaRunnerSmokeTest {

    private final ExpressionRunner runner = new ExpressionRunnerImpl();

    @BeforeAll
    static void installPlugins() {
        FormulaPluginRegistry.getInstance().installPluginRegistry(MathFunctionPlugin.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        runner.close();
    }

    @Test
    @DisplayName("编译并执行内置数学函数")
    void runMathFunction() {
        Double avg = runner.run("avg(1,2,3)");
        assertThat(avg).isEqualTo(2.0d);
    }

    @Test
    @DisplayName("同一表达式重复执行命中脚本缓存")
    void reusedCompiledClass() {
        String expression = "max(4,5,6)";
        assertThat(runner.<Double>run(expression)).isEqualTo(6.0d);
        assertThat(runner.<Double>run(expression)).isEqualTo(6.0d);
    }

    @Test
    @DisplayName("绑定变量以 \"#{name}\" 字符串形式参与计算")
    void runWithBinding() {
        Binding binding = new Binding();
        binding.setVariable("rate", -3);
        Double result = runner.run("abs(\"#{rate}\")", binding);
        assertThat(result).isEqualTo(3.0d);
    }

    @Test
    @DisplayName("未知函数在运行期抛出 FormulaRuntimeException")
    void unknownFunctionFails() {
        assertThatThrownBy(() -> runner.run("noSuchFunction(1)"))
                .isInstanceOf(FormulaRuntimeException.class)
                .rootCause()
                .hasMessageContaining("can not found method for function");
    }
}
