package com.swak.test.core.command;

/**
 * 供 {@code SwakMethodProviderTest} 使用的泛型夹具，用于生成 bridge 方法。
 * <p>
 * 这里刻意用顶层类：早期实现用 ASM5 读字节码，遇到嵌套类的 {@code NestHost} 属性会抛
 * {@link UnsupportedOperationException}；改用 ASM9 后两类都能读，嵌套场景由
 * {@code SwakMethodProviderTest#unbrideNestedClassSupported} 覆盖。
 */
interface GenericServiceFixture<T> {

    T value();
}

class StringServiceFixture implements GenericServiceFixture<String> {

    @Override
    public String value() {
        return "v";
    }
}

interface PrimitiveServiceFixture<T> {

    T convert(int code, String name);
}

/**
 * 基本类型参数：bridge 描述符为 {@code (ILjava/lang/String;)Ljava/lang/Object;}。
 */
class IntegerServiceFixture implements PrimitiveServiceFixture<Integer> {

    @Override
    public Integer convert(int code, String name) {
        return code;
    }
}

interface ArrayServiceFixture<T> {

    T join(String[] parts);
}

/**
 * 数组参数：bridge 描述符为 {@code ([Ljava/lang/String;)Ljava/lang/Object;}。
 */
class StringArrayServiceFixture implements ArrayServiceFixture<String> {

    @Override
    public String join(String[] parts) {
        return String.join(",", parts);
    }
}
