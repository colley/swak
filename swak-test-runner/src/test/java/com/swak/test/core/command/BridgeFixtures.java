package com.swak.test.core.command;

/**
 * 供 {@code SwakMethodProviderTest} 使用的顶层泛型夹具。
 * <p>
 * 必须是顶层类：生产代码 {@code SwakMethodProvider.unbride} 用 ASM5 读取 class 文件，
 * 遇到嵌套类的 {@code NestHost} 属性会直接抛 {@link UnsupportedOperationException}。
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
