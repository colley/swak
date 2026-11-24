package com.swak.test.core.command;

import com.swak.core.command.SwakMethodExecution;
import com.swak.core.command.annotation.ExtAsyncResult;
import com.swak.core.command.annotation.SwakExecutionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SwakMethodExecution} 的方法调用封装。
 * <p>
 * 同步分支走反射直调（包含私有方法），异步分支返回 {@link ExtAsyncResult}，
 * 由 AsyncClosureFactory 转成闭包后执行；异常需要还原成原始 cause。
 */
@DisplayName("Swak 方法执行封装")
class SwakMethodExecutionTest {

    static class Calculator {

        int add(int a, int b) {
            return a + b;
        }

        public String echo(String value) {
            return "echo:" + value;
        }

        public String fail(String reason) {
            throw new IllegalStateException("boom:" + reason);
        }

        private String secret() {
            return "secret";
        }

        public String plain() {
            return "plain";
        }

        public ExtAsyncResult<String> async() {
            return ExtAsyncResult.supplyAsync(() -> "async-value");
        }
    }

    private final Calculator calculator = new Calculator();

    @Test
    @DisplayName("两参构造：参数为空数组；三参构造保留原数组")
    void constructorsAndGetters() throws Exception {
        Method plain = Calculator.class.getDeclaredMethod("plain");

        SwakMethodExecution noArgs = new SwakMethodExecution(calculator, plain);
        assertThat(noArgs.getObject()).isSameAs(calculator);
        assertThat(noArgs.getMethod()).isSameAs(plain);
        assertThat(noArgs.getArgs()).isEmpty();

        Object[] args = {"x"};
        SwakMethodExecution withArgs = new SwakMethodExecution(calculator, plain, args);
        assertThat(withArgs.getArgs()).isSameAs(args);
    }

    @Test
    @DisplayName("同步执行：带参数调用，包级私有方法也可反射调用")
    void executeSyncWithArgs() throws Throwable {
        Method add = Calculator.class.getDeclaredMethod("add", int.class, int.class);
        SwakMethodExecution execution = new SwakMethodExecution(calculator, add, new Object[]{2, 3});

        assertThat(execution.execute(SwakExecutionType.SYNCHRONOUS)).isEqualTo(5);
    }

    @Test
    @DisplayName("executeWithArgs：覆盖构造时传入的参数")
    void executeWithOverrideArgs() throws Throwable {
        Method echo = Calculator.class.getDeclaredMethod("echo", String.class);
        SwakMethodExecution execution = new SwakMethodExecution(calculator, echo, new Object[]{"old"});

        assertThat(execution.execute(SwakExecutionType.SYNCHRONOUS)).isEqualTo("echo:old");
        assertThat(execution.executeWithArgs(SwakExecutionType.SYNCHRONOUS, new Object[]{"new"}))
                .isEqualTo("echo:new");
    }

    @Test
    @DisplayName("私有方法：setAccessible 后可正常执行")
    void executePrivateMethod() throws Throwable {
        Method secret = Calculator.class.getDeclaredMethod("secret");
        SwakMethodExecution execution = new SwakMethodExecution(calculator, secret);

        assertThat(execution.execute(SwakExecutionType.SYNCHRONOUS)).isEqualTo("secret");
    }

    @Test
    @DisplayName("异常：InvocationTargetException 会还原成原始 cause")
    void propagatesOriginalCause() throws Exception {
        Method fail = Calculator.class.getDeclaredMethod("fail", String.class);
        SwakMethodExecution execution = new SwakMethodExecution(calculator, fail, new Object[]{"oops"});

        assertThatThrownBy(() -> execution.execute(SwakExecutionType.SYNCHRONOUS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom:oops");
    }

    @Test
    @DisplayName("异步执行：返回值经闭包取到 supplier 的结果")
    void executeAsync() throws Throwable {
        Method async = Calculator.class.getDeclaredMethod("async");
        SwakMethodExecution execution = new SwakMethodExecution(calculator, async);

        assertThat(execution.execute(SwakExecutionType.ASYNCHRONOUS)).isEqualTo("async-value");
    }
}
