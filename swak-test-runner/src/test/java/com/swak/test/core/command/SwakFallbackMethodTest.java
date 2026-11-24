package com.swak.test.core.command;

import com.swak.core.command.annotation.ExtAsyncResult;
import com.swak.core.command.annotation.SwakExecutionType;
import com.swak.core.command.annotation.SwakFallback;
import com.swak.core.command.exception.SwakFallBackException;
import com.swak.core.command.fallback.SwakFallbackMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SwakFallbackMethod} 的返回类型校验与实际执行。
 * <p>
 * 校验规则较多（同步/异步、普通类型/参数化类型、兜底本身是否为 command），
 * 每条规则都单独钉一个用例；不兼容时统一抛 {@link SwakFallBackException}。
 */
@DisplayName("Swak 兜底方法")
class SwakFallbackMethodTest {

    static class Client {

        public String fallback() {
            return "fb";
        }

        public String throwingFallback() {
            throw new IllegalStateException("fb-boom");
        }

        public ExtAsyncResult<String> asyncFallback() {
            return ExtAsyncResult.supplyAsync(() -> "async-fb");
        }
    }

    @SuppressWarnings("unused")
    static class Types {

        public String string() {
            return "";
        }

        public Object object() {
            return null;
        }

        public List<String> stringList() {
            return null;
        }

        public List<Integer> intList() {
            return null;
        }

        public Future<String> stringFuture() {
            return null;
        }

        public Future<Integer> intFuture() {
            return null;
        }

        @SwakFallback(fallbackMethod = "any")
        public Future<String> annotatedStringFuture() {
            return null;
        }

        @SwakFallback(fallbackMethod = "any")
        public Future<Integer> annotatedIntFuture() {
            return null;
        }
    }

    @SuppressWarnings("unused")
    static class AnnotationHolder {

        @SwakFallback(fallbackMethod = "any")
        public void sync() {
        }

        @SwakFallback(fallbackMethod = "any", async = SwakExecutionType.ASYNCHRONOUS)
        public void async() {
        }
    }

    // ---------------------------------------------------------------- 基础属性

    @Test
    @DisplayName("ABSENT：不存在任何方法")
    void absent() {
        assertThat(SwakFallbackMethod.ABSENT.isPresent()).isFalse();
        assertThat(SwakFallbackMethod.ABSENT.getMethod()).isNull();
        assertThat(SwakFallbackMethod.ABSENT.getExecutionType()).isNull();
        assertThat(SwakFallbackMethod.ABSENT.isCommand()).isFalse();
    }

    @Test
    @DisplayName("单参构造：默认兜底方法名 getFallback，同步类型")
    void singleArgConstructor() throws Exception {
        Method fallback = Client.class.getDeclaredMethod("fallback");

        SwakFallbackMethod method = new SwakFallbackMethod(fallback);

        assertThat(method.isPresent()).isTrue();
        assertThat(method.getFallbackMethod()).isEqualTo("getFallback");
        assertThat(method.getExecutionType()).isEqualTo(SwakExecutionType.SYNCHRONOUS);
        assertThat(method.isExtended()).isFalse();
        assertThat(method.isCommand()).isFalse();
    }

    @Test
    @DisplayName("setFallbackMethod 可改写兜底方法名")
    void setFallbackMethod() throws Exception {
        SwakFallbackMethod method =
                new SwakFallbackMethod(Client.class.getDeclaredMethod("fallback"));

        method.setFallbackMethod("other");

        assertThat(method.getFallbackMethod()).isEqualTo("other");
    }

    @Test
    @DisplayName("返回 Future 的兜底：执行类型判定为 ASYNCHRONOUS")
    void asyncExecutionType() throws Exception {
        SwakFallbackMethod method =
                new SwakFallbackMethod(Client.class.getDeclaredMethod("asyncFallback"));

        assertThat(method.getExecutionType()).isEqualTo(SwakExecutionType.ASYNCHRONOUS);
    }

    // ---------------------------------------------------------------- 执行

    @Test
    @DisplayName("同步执行：反射调用兜底方法")
    void executeSyncFallback() throws Throwable {
        SwakFallbackMethod method =
                new SwakFallbackMethod(Client.class.getDeclaredMethod("fallback"));

        assertThat(method.executeWithArgs(new Client())).isEqualTo("fb");
    }

    @Test
    @DisplayName("同步执行：兜底自身抛异常时原样向外抛")
    void executeSyncFallbackThatThrows() throws Exception {
        SwakFallbackMethod method =
                new SwakFallbackMethod(Client.class.getDeclaredMethod("throwingFallback"));

        assertThatThrownBy(() -> method.executeWithArgs(new Client()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("fb-boom");
    }

    @Test
    @DisplayName("异步执行：通过 ExtAsyncResult 闭包取到 supplier 结果")
    void executeAsyncFallback() throws Throwable {
        SwakFallbackMethod method =
                new SwakFallbackMethod(Client.class.getDeclaredMethod("asyncFallback"));

        assertThat(method.executeWithArgs(new Client())).isEqualTo("async-fb");
    }

    // ---------------------------------------------------------------- 返回类型校验

    @Test
    @DisplayName("同步：返回类型兼容时通过")
    void validateCompatibleSyncTypes() throws Exception {
        SwakFallbackMethod fallback = fallback("string", sync());

        fallback.validateReturnType(method("string"));
    }

    @Test
    @DisplayName("同步：兜底返回父类型（Object）不兼容，抛异常")
    void validateIncompatiblePlainTypes() throws Exception {
        SwakFallbackMethod fallback = fallback("object", sync());

        assertThatThrownBy(() -> fallback.validateReturnType(method("string")))
                .isInstanceOf(SwakFallBackException.class)
                .hasMessageContaining("Incompatible return types");
    }

    @Test
    @DisplayName("参数化类型不一致：List<Integer> 对 List<String> 抛异常")
    void validateWrongParametrizedType() throws Exception {
        SwakFallbackMethod fallback = fallback("intList", sync());

        assertThatThrownBy(() -> fallback.validateReturnType(method("stringList")))
                .isInstanceOf(SwakFallBackException.class)
                .hasMessageContaining("Incompatible return types");
    }

    @Test
    @DisplayName("参数化类型数量不一致：List<String> 对 String 抛异常")
    void validateDifferentParametrizedCount() throws Exception {
        SwakFallbackMethod fallback = fallback("string", sync());

        assertThatThrownBy(() -> fallback.validateReturnType(method("stringList")))
                .isInstanceOf(SwakFallBackException.class)
                .hasMessageContaining("Incompatible return types");
    }

    @Test
    @DisplayName("异步命令 + 普通兜底：当前实现允许（只校验泛型参数）")
    void validateAsyncCommandWithPlainFallback() throws Exception {
        SwakFallbackMethod fallback = fallback("string", sync());

        // 既有行为：Future<String> 命令允许返回 String 的兜底
        fallback.validateReturnType(method("stringFuture"));
    }

    @Test
    @DisplayName("异步命令 + 异步兜底但兜底不是 command：拒绝")
    void validateAsyncCommandWithNonCommandAsyncFallback() throws Exception {
        SwakFallbackMethod fallback = fallback("stringFuture", sync());

        assertThatThrownBy(() -> fallback.validateReturnType(method("stringFuture")))
                .isInstanceOf(SwakFallBackException.class)
                .hasMessageContaining("Incompatible return types");
    }

    @Test
    @DisplayName("异步命令 + 异步兜底且兜底是 command：泛型一致则通过")
    void validateAsyncCommandWithCommandAsyncFallback() throws Exception {
        SwakFallbackMethod fallback = fallback("annotatedStringFuture", sync());

        assertThat(fallback.isCommand()).isTrue();
        fallback.validateReturnType(method("stringFuture"));
    }

    @Test
    @DisplayName("异步命令 + command 兜底但泛型不一致：抛异常")
    void validateAsyncCommandWithCommandAsyncFallbackMismatch() throws Exception {
        SwakFallbackMethod fallback = fallback("annotatedIntFuture", sync());

        assertThatThrownBy(() -> fallback.validateReturnType(method("stringFuture")))
                .isInstanceOf(SwakFallBackException.class)
                .hasMessageContaining("Incompatible return types");
    }

    @Test
    @DisplayName("同步命令 + 返回 Future 的兜底：拒绝")
    void validateSyncCommandWithFutureFallback() throws Exception {
        SwakFallbackMethod fallback = fallback("stringFuture", sync());

        assertThatThrownBy(() -> fallback.validateReturnType(method("string")))
                .isInstanceOf(SwakFallBackException.class)
                .hasMessageContaining("Incompatible return types");
    }

    @Test
    @DisplayName("@SwakFallback(async = ASYNCHRONOUS)：同步方法也按异步命令校验，普通兜底可通过")
    void validateWithAsyncAnnotation() throws Exception {
        SwakFallbackMethod fallback = fallback("string", async());

        fallback.validateReturnType(method("string"));
    }

    @Test
    @DisplayName("已知缺陷：异常信息只拼了 \"Hint: \" 前缀，具体原因没有输出")
    void errorMessageDropsHintContent() throws Exception {
        SwakFallbackMethod fallback = fallback("stringFuture", sync());

        assertThatThrownBy(() -> fallback.validateReturnType(method("string")))
                .isInstanceOf(SwakFallBackException.class)
                .hasMessageContaining("Hint: ")
                .hasMessageNotContaining("fallback cannot return Future");
    }

    @Test
    @DisplayName("ABSENT 调用 validateReturnType 直接返回，不做任何校验")
    void validateAbsentDoesNothing() throws Exception {
        SwakFallbackMethod.ABSENT.validateReturnType(method("string"));
    }

    private static Method method(String name) throws NoSuchMethodException {
        return Types.class.getMethod(name);
    }

    private static SwakFallbackMethod fallback(String methodName, SwakFallback annotation)
            throws NoSuchMethodException {
        return new SwakFallbackMethod(Types.class.getMethod(methodName), methodName, false, annotation);
    }

    private static SwakFallback sync() throws NoSuchMethodException {
        return AnnotationHolder.class.getMethod("sync").getAnnotation(SwakFallback.class);
    }

    private static SwakFallback async() throws NoSuchMethodException {
        return AnnotationHolder.class.getMethod("async").getAnnotation(SwakFallback.class);
    }
}
