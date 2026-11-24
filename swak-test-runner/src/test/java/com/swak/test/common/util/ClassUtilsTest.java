package com.swak.test.common.util;

import com.swak.common.exception.ArgumentException;
import com.swak.common.exception.SwakException;
import com.swak.common.util.ClassUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ClassUtils} 的类名解析、实例化与反射辅助回归。
 */
@DisplayName("ClassUtils 类工具")
class ClassUtilsTest {

    static class PrivateCtorBean {

        private PrivateCtorBean() {
        }
    }

    static class NoDefaultCtorBean {

        NoDefaultCtorBean(String name) {
        }
    }

    static class GenericHolder<T extends Number> {

        List<String> names;

        List<? extends Number> wildcard;

        T value;
    }

    static class MultiBoundHolder<T extends Number & Comparable<T>> {

        T value;
    }

    @Test
    @DisplayName("forName：普通类、基本类型、基本类型数组、对象数组、内部数组名")
    void forNameVariants() throws Exception {
        assertThat(ClassUtils.forName("java.lang.String")).isEqualTo(String.class);
        assertThat(ClassUtils.forName("int")).isEqualTo(int.class);
        assertThat(ClassUtils.forName("int[]")).isEqualTo(int[].class);
        assertThat(ClassUtils.forName("[I")).isEqualTo(int[].class);
        assertThat(ClassUtils.forName("java.lang.String[]")).isEqualTo(String[].class);
        assertThat(ClassUtils.forName("[Ljava.lang.String;")).isEqualTo(String[].class);
        // classLoader 传 null 时回落到默认 ClassLoader
        assertThat(ClassUtils.forName("java.lang.String", null)).isEqualTo(String.class);
    }

    @Test
    @DisplayName("forName：不存在的类抛 ClassNotFoundException")
    void forNameMissing() {
        assertThatThrownBy(() -> ClassUtils.forName("com.swak.not.Exists"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("forName 的两种 ClassLoader 入口")
    void forNameWithLoaders() throws Exception {
        assertThat(ClassUtils.forNameWithThreadContextClassLoader("java.lang.String")).isEqualTo(String.class);
        assertThat(ClassUtils.forNameWithCallerClassLoader("java.lang.String", ClassUtils.class))
                .isEqualTo(String.class);
        assertThat(ClassUtils.getCallerClassLoader(String.class)).isNull();
    }

    @Test
    @DisplayName("resolvePrimitiveClassName：只识别 8 位以内的基本类型名")
    void resolvePrimitiveClassName() {
        assertThat(ClassUtils.resolvePrimitiveClassName("boolean")).isEqualTo(boolean.class);
        assertThat(ClassUtils.resolvePrimitiveClassName("double")).isEqualTo(double.class);
        assertThat(ClassUtils.resolvePrimitiveClassName("[J")).isEqualTo(long[].class);
        assertThat(ClassUtils.resolvePrimitiveClassName(null)).isNull();
        assertThat(ClassUtils.resolvePrimitiveClassName("java.lang.String")).isNull();
        assertThat(ClassUtils.resolvePrimitiveClassName("not-int")).isNull();
    }

    @Test
    @DisplayName("getClassLoader：上下文 ClassLoader 优先，结果非空")
    void classLoader() {
        assertThat(ClassUtils.getClassLoader()).isNotNull();
        assertThat(ClassUtils.getClassLoader(ClassUtils.class)).isNotNull();
        assertThat(ClassUtils.getDefaultClassLoader()).isNotNull();
    }

    @Test
    @DisplayName("toShortString：null 与普通对象的简写")
    void toShortString() {
        assertThat(ClassUtils.toShortString(null)).isEqualTo("null");

        Object o = new Object();
        assertThat(ClassUtils.toShortString(o))
                .startsWith("Object@")
                .endsWith(String.valueOf(System.identityHashCode(o)));
    }

    @Test
    @DisplayName("simpleClassName：取最后一段类名，null 抛 NPE")
    void simpleClassName() {
        assertThat(ClassUtils.simpleClassName(String.class)).isEqualTo("String");
        assertThat(ClassUtils.simpleClassName(int[].class)).isEqualTo("[I");
        assertThatThrownBy(() -> ClassUtils.simpleClassName(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("isPrimitive：基本类型、包装类型、String、Object 都算")
    void isPrimitive() {
        assertThat(ClassUtils.isPrimitive(int.class)).isTrue();
        assertThat(ClassUtils.isPrimitive(Integer.class)).isTrue();
        assertThat(ClassUtils.isPrimitive(String.class)).isTrue();
        assertThat(ClassUtils.isPrimitive(Object.class)).isTrue();
        assertThat(ClassUtils.isPrimitive(List.class)).isFalse();
    }

    @Test
    @DisplayName("convertPrimitive：各基本类型转换与非法数字回退 null")
    void convertPrimitive() {
        assertThat(ClassUtils.convertPrimitive(int.class, null)).isNull();
        assertThat(ClassUtils.convertPrimitive(char.class, "abc")).isEqualTo('a');
        assertThat(ClassUtils.convertPrimitive(Character.class, "")).isEqualTo('\0');
        assertThat(ClassUtils.convertPrimitive(boolean.class, "true")).isEqualTo(true);
        assertThat(ClassUtils.convertPrimitive(byte.class, "1")).isEqualTo((byte) 1);
        assertThat(ClassUtils.convertPrimitive(short.class, "2")).isEqualTo((short) 2);
        assertThat(ClassUtils.convertPrimitive(int.class, "3")).isEqualTo(3);
        assertThat(ClassUtils.convertPrimitive(long.class, "4")).isEqualTo(4L);
        assertThat(ClassUtils.convertPrimitive(float.class, "1.5")).isEqualTo(1.5f);
        assertThat(ClassUtils.convertPrimitive(double.class, "2.5")).isEqualTo(2.5d);
        assertThat(ClassUtils.convertPrimitive(int.class, "x")).isNull();
        // 非基本类型直接原样返回
        assertThat(ClassUtils.convertPrimitive(String.class, "x")).isEqualTo("x");
    }

    @Test
    @DisplayName("isTypeMatch：只对布尔类型做严格校验")
    void isTypeMatch() {
        assertThat(ClassUtils.isTypeMatch(boolean.class, "true")).isTrue();
        assertThat(ClassUtils.isTypeMatch(Boolean.class, "false")).isTrue();
        assertThat(ClassUtils.isTypeMatch(Boolean.class, "yes")).isFalse();
        assertThat(ClassUtils.isTypeMatch(boolean.class, null)).isFalse();
        assertThat(ClassUtils.isTypeMatch(Integer.class, "whatever")).isTrue();
    }

    @Test
    @DisplayName("getClass(Type)：Class / ParameterizedType / TypeVariable / WildcardType")
    void getClassFromType() throws Exception {
        assertThat(ClassUtils.getClass(null)).isNull();
        assertThat(ClassUtils.getClass(String.class)).isEqualTo(String.class);

        Type names = GenericHolder.class.getDeclaredField("names").getGenericType();
        assertThat(ClassUtils.getClass(names)).isEqualTo(List.class);

        Type value = GenericHolder.class.getDeclaredField("value").getGenericType();
        assertThat(ClassUtils.getClass(value)).isEqualTo(Number.class);

        Type wildcard = ((ParameterizedType) GenericHolder.class.getDeclaredField("wildcard").getGenericType())
                .getActualTypeArguments()[0];
        assertThat(ClassUtils.getClass(wildcard)).isEqualTo(Number.class);

        Type multiBound = MultiBoundHolder.class.getDeclaredField("value").getGenericType();
        assertThat(ClassUtils.getClass(multiBound)).as("多边界类型无法唯一确定，返回 null").isNull();
    }

    @Test
    @DisplayName("isProxy / getUserClass：普通类原样返回，null 抛 ArgumentException")
    void proxyAndUserClass() {
        assertThat(ClassUtils.isProxy(null)).isFalse();
        assertThat(ClassUtils.isProxy(Serializable.class)).isFalse();

        assertThat(ClassUtils.getUserClass(String.class)).isEqualTo(String.class);
        assertThat(ClassUtils.getUserClass((Object) "x")).isEqualTo(String.class);

        assertThatThrownBy(() -> ClassUtils.getUserClass((Class<?>) null))
                .isInstanceOf(ArgumentException.class);
        assertThatThrownBy(() -> ClassUtils.getUserClass((Object) null))
                .isInstanceOf(ArgumentException.class);
    }

    @Test
    @DisplayName("isBoolean：原生与包装类型")
    void isBoolean() {
        assertThat(ClassUtils.isBoolean(boolean.class)).isTrue();
        assertThat(ClassUtils.isBoolean(Boolean.class)).isTrue();
        assertThat(ClassUtils.isBoolean(int.class)).isFalse();
    }

    @Test
    @DisplayName("newInstance：私有构造可通过反射实例化，缺少无参构造抛 SwakException")
    void newInstance() {
        assertThat(ClassUtils.newInstance(PrivateCtorBean.class)).isInstanceOf(PrivateCtorBean.class);
        assertThat(ClassUtils.<PrivateCtorBean>newInstance(PrivateCtorBean.class.getName()))
                .isInstanceOf(PrivateCtorBean.class);

        assertThatThrownBy(() -> ClassUtils.newInstance(NoDefaultCtorBean.class))
                .isInstanceOf(SwakException.class)
                .hasMessageContaining("无参的构造方法");
    }

    @Test
    @DisplayName("toClassConfident：找不到类时抛 SwakException")
    void toClassConfident() {
        assertThat(ClassUtils.toClassConfident("java.lang.String")).isEqualTo(String.class);
        assertThat(ClassUtils.toClassConfident("java.lang.String", ClassUtils.class.getClassLoader()))
                .isEqualTo(String.class);
        assertThat(ClassUtils.toClassConfident("java.lang.String", null)).isEqualTo(String.class);

        assertThatThrownBy(() -> ClassUtils.toClassConfident("com.swak.not.Exists"))
                .isInstanceOf(SwakException.class);
    }

    @Test
    @DisplayName("getPackageName：有包名、默认包、null")
    void packageName() {
        assertThat(ClassUtils.getPackageName(ClassUtils.class)).isEqualTo("com.swak.common.util");
        assertThat(ClassUtils.getPackageName("java.lang.String")).isEqualTo("java.lang");
        assertThat(ClassUtils.getPackageName("String")).isEmpty();

        assertThatThrownBy(() -> ClassUtils.getPackageName((Class<?>) null))
                .isInstanceOf(ArgumentException.class);
        assertThatThrownBy(() -> ClassUtils.getPackageName((String) null))
                .isInstanceOf(ArgumentException.class);
    }

    @Test
    @DisplayName("ARRAY_SUFFIX 常量")
    void constants() {
        assertThat(ClassUtils.ARRAY_SUFFIX).isEqualTo("[]");
    }
}
