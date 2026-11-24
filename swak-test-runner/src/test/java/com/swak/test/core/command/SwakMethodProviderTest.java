package com.swak.test.core.command;

import com.google.common.base.Optional;
import com.swak.core.command.SwakMethodProvider;
import com.swak.core.command.annotation.SwakExecutionType;
import com.swak.core.command.annotation.SwakFallback;
import com.swak.core.command.exception.SwakFallBackException;
import com.swak.core.command.fallback.SwakFallbackMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SwakMethodProvider} 的兜底方法查找与桥接方法还原。
 * <p>
 * 覆盖点：无参兜底、带 Throwable 参数的扩展兜底、注解缺失/名称为空/方法不存在、
 * 私有方法不参与解析、父类方法查找，以及泛型接口生成的 bridge 方法还原。
 */
@DisplayName("Swak 兜底方法提供者")
class SwakMethodProviderTest {

    @SuppressWarnings("unused")
    static class OrderService {

        @SwakFallback(fallbackMethod = "fallback")
        public String query() {
            return "ok";
        }

        public String fallback() {
            return "fallback";
        }

        @SwakFallback(fallbackMethod = "fallbackWithThrowable")
        public String queryWithArg(String id) {
            return id;
        }

        @SwakFallback(fallbackMethod = "fallbackWithThrowable")
        public String queryWithThrowable(String id, Throwable throwable) {
            return id;
        }

        public String fallbackWithThrowable(String id, Throwable throwable) {
            return id + ":" + (throwable == null ? "none" : throwable.getMessage());
        }

        @SwakFallback
        public String blankFallback() {
            return "ok";
        }

        @SwakFallback(fallbackMethod = "notExist")
        public String missingFallback() {
            return "ok";
        }

        @SwakFallback(fallbackMethod = "fallback")
        private String privateFallback() {
            return "ok";
        }
    }

    static class ChildService extends OrderService {
    }

    interface NestedGeneric<T> {

        T value();
    }

    static class NestedStringService implements NestedGeneric<String> {

        @Override
        public String value() {
            return "v";
        }
    }

    private final SwakMethodProvider provider = SwakMethodProvider.getInstance();

    @Test
    @DisplayName("getInstance：单例")
    void singleton() {
        assertThat(SwakMethodProvider.getInstance()).isSameAs(provider);
    }

    @Test
    @DisplayName("无参兜底：isPresent，未扩展，执行类型为同步")
    void findsNoArgFallback() throws Exception {
        Method command = OrderService.class.getDeclaredMethod("query");

        SwakFallbackMethod fallback = provider.getFallbackMethod(OrderService.class, command);

        assertThat(fallback.isPresent()).isTrue();
        assertThat(fallback.isExtended()).isFalse();
        assertThat(fallback.isCommand()).isFalse();
        assertThat(fallback.getFallbackMethod()).isEqualTo("fallback");
        assertThat(fallback.getMethod().getName()).isEqualTo("fallback");
        assertThat(fallback.getExecutionType()).isEqualTo(SwakExecutionType.SYNCHRONOUS);
    }

    @Test
    @DisplayName("扩展兜底：优先匹配 (参数..., Throwable) 形式并标记 extended")
    void findsExtendedFallback() throws Exception {
        Method command = OrderService.class.getDeclaredMethod("queryWithArg", String.class);

        SwakFallbackMethod fallback = provider.getFallbackMethod(OrderService.class, command);

        assertThat(fallback.isExtended()).isTrue();
        assertThat(fallback.getMethod().getParameterTypes())
                .containsExactly(String.class, Throwable.class);
    }

    @Test
    @DisplayName("extended=true 且命令方法末参为 Throwable 时先剔除再查找")
    void findsExtendedFallbackWhenLastParameterIsThrowable() throws Exception {
        Method command = OrderService.class.getDeclaredMethod(
                "queryWithThrowable", String.class, Throwable.class);

        SwakFallbackMethod fallback = provider.getFallbackMethod(OrderService.class, command, true);

        assertThat(fallback.isPresent()).isTrue();
        assertThat(fallback.isExtended()).isTrue();
        assertThat(fallback.getMethod().getParameterTypes())
                .containsExactly(String.class, Throwable.class);
    }

    @Test
    @DisplayName("没有 @SwakFallback / 名称为空：返回 ABSENT")
    void absentWhenAnnotationMissingOrBlank() throws Exception {
        Method blank = OrderService.class.getDeclaredMethod("blankFallback");
        Method noAnnotation = OrderService.class.getDeclaredMethod("fallback");

        assertThat(provider.getFallbackMethod(OrderService.class, blank))
                .isSameAs(SwakFallbackMethod.ABSENT);
        assertThat(provider.getFallbackMethod(OrderService.class, noAnnotation))
                .isSameAs(SwakFallbackMethod.ABSENT);
    }

    @Test
    @DisplayName("私有命令方法不参与解析：返回 ABSENT")
    void absentForNonPublicMethod() throws Exception {
        Method privateMethod = OrderService.class.getDeclaredMethod("privateFallback");

        assertThat(provider.getFallbackMethod(OrderService.class, privateMethod))
                .isSameAs(SwakFallbackMethod.ABSENT);
    }

    @Test
    @DisplayName("兜底方法不存在：抛 SwakFallBackException 并带上方法签名")
    void throwsWhenFallbackMissing() throws Exception {
        Method command = OrderService.class.getDeclaredMethod("missingFallback");

        assertThatThrownBy(() -> provider.getFallbackMethod(OrderService.class, command))
                .isInstanceOf(SwakFallBackException.class)
                .hasMessageContaining("fallback method wasn't found")
                .hasMessageContaining("notExist");
    }

    @Test
    @DisplayName("getMethod：本类找不到时向父类递归查找")
    void getMethodSearchesSuperclass() throws Exception {
        Optional<Method> method = provider.getMethod(ChildService.class, "fallback", new Class<?>[0]);

        assertThat(method.isPresent()).isTrue();
        assertThat(method.get().getDeclaringClass()).isEqualTo(OrderService.class);
    }

    @Test
    @DisplayName("getMethod：参数不匹配 / 方法不存在时返回 absent")
    void getMethodAbsent() throws Exception {
        Optional<Method> wrongParams =
                provider.getMethod(OrderService.class, "fallback", String.class);
        Optional<Method> missing =
                provider.getMethod(OrderService.class, "notExist");

        assertThat(wrongParams.isPresent()).isFalse();
        assertThat(missing.isPresent()).isFalse();
    }

    @Test
    @DisplayName("unbride：bridge 方法还原为泛型方法，且结果被缓存")
    void unbrideBridgeMethod() throws Exception {
        Method bridge = findBridge(StringServiceFixture.class);

        Method generic = provider.unbride(bridge, StringServiceFixture.class);
        assertThat(generic.getName()).isEqualTo("value");
        assertThat(generic.getReturnType()).isEqualTo(String.class);
        assertThat(generic.isBridge()).isFalse();

        // 第二次命中内部缓存，返回同一个 Method
        assertThat(provider.unbride(bridge, StringServiceFixture.class)).isSameAs(generic);
    }

    @Test
    @DisplayName("unbride：非 bridge 方法原样返回")
    void unbrideNonBridgeMethod() throws Exception {
        Method method = StringServiceFixture.class.getDeclaredMethod("value");

        assertThat(provider.unbride(method, StringServiceFixture.class)).isSameAs(method);
    }

    @Test
    @DisplayName("unbride：嵌套类的 bridge 方法也能还原（ASM9 可读 NestHost 属性）")
    void unbrideNestedClassSupported() throws Exception {
        Method bridge = findBridge(NestedStringService.class);

        Method generic = provider.unbride(bridge, NestedStringService.class);

        assertThat(generic.getName()).isEqualTo("value");
        assertThat(generic.getReturnType()).isEqualTo(String.class);
        assertThat(generic.isBridge()).isFalse();
    }

    @Test
    @DisplayName("unbride：基本类型参数的 bridge 描述符能正确数出参数个数")
    void unbrideBridgeWithPrimitiveParameters() throws Exception {
        Method bridge = findBridge(IntegerServiceFixture.class);
        assertThat(bridge.getParameterTypes()).containsExactly(int.class, String.class);

        Method generic = provider.unbride(bridge, IntegerServiceFixture.class);

        assertThat(generic.getParameterTypes()).containsExactly(int.class, String.class);
        assertThat(generic.getReturnType()).isEqualTo(Integer.class);
    }

    @Test
    @DisplayName("unbride：数组参数的 bridge 方法能正确解析为数组类型")
    void unbrideBridgeWithArrayParameter() throws Exception {
        Method bridge = findBridge(StringArrayServiceFixture.class);
        assertThat(bridge.getParameterTypes()).containsExactly(String[].class);

        Method generic = provider.unbride(bridge, StringArrayServiceFixture.class);

        assertThat(generic.getParameterTypes()).containsExactly(String[].class);
        assertThat(generic.getReturnType()).isEqualTo(String.class);
    }

    private static Method findBridge(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(Method::isBridge)
                .findFirst()
                .orElseThrow(() -> new AssertionError("没有找到 bridge 方法: " + type));
    }
}
