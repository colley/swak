package com.swak.jdbc.conditions;

@FunctionalInterface
public interface WrapperFunction<T> {

    T apply(T wrapper);
}
