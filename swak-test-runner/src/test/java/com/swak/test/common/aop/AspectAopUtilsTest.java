package com.swak.test.common.aop;

import com.google.common.base.Optional;
import com.swak.common.aop.AspectAopUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link AspectAopUtils} 的反射辅助回归。
 * <p>
 * 这里不依赖任何 AOP 代理框架，直接手写一个最小的 {@link ProceedingJoinPoint}，
 * 保证 {@code getTarget()} 返回真实对象、{@code getSignature()} 返回真实 Method。
 */
@DisplayName("AspectAopUtils 切面工具")
class AspectAopUtilsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface Marker {
        String value() default "";
    }

    static class TargetService {

        @Marker("hello")
        public String greet(String name) {
            return "hi " + name;
        }

        public void ping() {
        }

        public void risky() throws IOException {
        }

        private void hidden() {
        }
    }

    static class InheritedService extends TargetService {
    }

    @Marker("type")
    static class AnnotatedService {

        public void run() {
        }
    }

    static class SubAnnotatedService extends AnnotatedService {
    }

    static class FakeSignature implements MethodSignature {

        private final Method method;

        FakeSignature(Method method) {
            this.method = method;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public String[] getParameterNames() {
            return new String[0];
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }

        @Override
        public Class<?> getReturnType() {
            return method.getReturnType();
        }

        @Override
        public Class<?> getDeclaringType() {
            return method.getDeclaringClass();
        }

        @Override
        public String getDeclaringTypeName() {
            return method.getDeclaringClass().getName();
        }

        @Override
        public String getName() {
            return method.getName();
        }

        @Override
        public int getModifiers() {
            return method.getModifiers();
        }

        @Override
        public Class<?>[] getExceptionTypes() {
            return method.getExceptionTypes();
        }

        @Override
        public String toShortString() {
            return method.getName();
        }

        @Override
        public String toLongString() {
            return method.toGenericString();
        }
    }

    static class FakeJoinPoint implements ProceedingJoinPoint {

        private final Object target;

        private final Method method;

        private final Object[] args;

        FakeJoinPoint(Object target, Method method, Object... args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }

        @Override
        public Object proceed() {
            return "proceeded";
        }

        @Override
        public Object proceed(Object[] args) {
            return "proceeded";
        }

        @Override
        public void set$AroundClosure(org.aspectj.runtime.internal.AroundClosure arc) {
            // 单测不涉及 around advice，忽略闭包注入
        }

        @Override
        public String toShortString() {
            return getKind();
        }

        @Override
        public String toLongString() {
            return getKind();
        }

        @Override
        public Object getThis() {
            return target;
        }

        @Override
        public Object getTarget() {
            return target;
        }

        @Override
        public Object[] getArgs() {
            return args;
        }

        @Override
        public Signature getSignature() {
            return new FakeSignature(method);
        }

        @Override
        public SourceLocation getSourceLocation() {
            return null;
        }

        @Override
        public String getKind() {
            return "method-execution";
        }

        @Override
        public JoinPoint.StaticPart getStaticPart() {
            return null;
        }
    }

    private static Method greetMethod() throws NoSuchMethodException {
        return TargetService.class.getDeclaredMethod("greet", String.class);
    }

    @Test
    @DisplayName("getParameterTypes / getMethodFromTarget：从 JoinPoint 反查真实 Method")
    void methodFromJoinPoint() throws Exception {
        Method method = greetMethod();
        FakeJoinPoint joinPoint = new FakeJoinPoint(new TargetService(), method, "swak");

        assertThat(AspectAopUtils.getParameterTypes(joinPoint)).containsExactly(String.class);
        assertThat(AspectAopUtils.getMethodFromTarget(joinPoint)).isEqualTo(method);
        assertThat(AspectAopUtils.getMethodFromTarget(joinPoint, "greet")).isEqualTo(method);
        assertThat(AspectAopUtils.getMethodFromTarget(joinPoint, "notExists")).isNull();
        assertThat(AspectAopUtils.getMethodFromTarget(new FakeJoinPoint(new TargetService(), method), "greet"))
                .isEqualTo(method);
    }

    @Test
    @DisplayName("getDeclaredMethod：本类、父类继承、Object 继承与不存在")
    void getDeclaredMethod() throws Exception {
        Method greet = greetMethod();

        assertThat(AspectAopUtils.getDeclaredMethod(TargetService.class, "greet", String.class)).isEqualTo(greet);
        assertThat(AspectAopUtils.getDeclaredMethod(InheritedService.class, "greet", String.class)).isEqualTo(greet);
        assertThat(AspectAopUtils.getDeclaredMethod(TargetService.class, "toString")).isNotNull();
        assertThat(AspectAopUtils.getDeclaredMethod(TargetService.class, "greet", int.class)).isNull();
    }

    @Test
    @DisplayName("getAnnotation(ProceedingJoinPoint)：取到方法上的注解")
    void annotationFromProceedingJoinPoint() throws Exception {
        FakeJoinPoint joinPoint = new FakeJoinPoint(new TargetService(), greetMethod(), "swak");

        Marker marker = AspectAopUtils.getAnnotation((ProceedingJoinPoint) joinPoint, Marker.class);

        assertThat(marker).isNotNull();
        assertThat(marker.value()).isEqualTo("hello");
        assertThat(AspectAopUtils.getAnnotation(
                new FakeJoinPoint(new TargetService(), TargetService.class.getDeclaredMethod("ping")),
                Marker.class)).isNull();
    }

    @Test
    @DisplayName("getAnnotation(JoinPoint, Class)：查的是 target 的类注解")
    void annotationFromJoinPointAndClass() throws Exception {
        FakeJoinPoint annotated = new FakeJoinPoint(
                new AnnotatedService(), AnnotatedService.class.getDeclaredMethod("run"));

        Optional<Marker> typeAnnotation = AspectAopUtils.getAnnotation((JoinPoint) annotated, Marker.class);
        assertThat(typeAnnotation.isPresent()).isTrue();
        assertThat(typeAnnotation.get().value()).isEqualTo("type");

        // TargetService 的注解在方法上而不在类上，这里应该查不到
        FakeJoinPoint plain = new FakeJoinPoint(new TargetService(), greetMethod(), "swak");
        assertThat(AspectAopUtils.getAnnotation((JoinPoint) plain, Marker.class).isPresent()).isFalse();

        assertThat(AspectAopUtils.getAnnotation(AnnotatedService.class, Marker.class).get().value())
                .isEqualTo("type");
        // 注解在父类上时递归向上查找
        assertThat(AspectAopUtils.getAnnotation(SubAnnotatedService.class, Marker.class).get().value())
                .isEqualTo("type");
        assertThat(AspectAopUtils.getAnnotation(String.class, Marker.class).isPresent()).isFalse();
    }

    @Test
    @DisplayName("getAnnotation(Class, Class)：参数为 null 抛 NullPointerException")
    void annotationNullValidation() {
        assertThatThrownBy(() -> AspectAopUtils.getAnnotation((Class<?>) null, Marker.class))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("type cannot be null");
        assertThatThrownBy(() -> AspectAopUtils.getAnnotation(TargetService.class, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("annotation cannot be null");
    }

    @Test
    @DisplayName("getMethodInfo：无参、有参、异常、私有与注解的文本输出")
    void methodInfo() throws Exception {
        Method greet = greetMethod();
        String info = AspectAopUtils.getMethodInfo(greet);

        assertThat(info)
                .contains("Method signature:")
                .contains("Declaring class:")
                .contains("AccessLevel=public")
                .contains("ReturnType=class java.lang.String")
                .contains("Parameters:")
                .contains("parameter [0]:")
                .contains("GenericParameterType=class java.lang.String")
                .contains("Annotations:")
                .contains("annotation[0]=");

        String ping = AspectAopUtils.getMethodInfo(TargetService.class.getDeclaredMethod("ping"));
        assertThat(ping).contains("Parameters:empty").contains("Exceptions:empty");

        String risky = AspectAopUtils.getMethodInfo(TargetService.class.getDeclaredMethod("risky"));
        assertThat(risky).contains("exception [0]:").contains("java.io.IOException");

        String hidden = AspectAopUtils.getMethodInfo(TargetService.class.getDeclaredMethod("hidden"));
        assertThat(hidden).contains("AccessLevel=private");
    }
}
