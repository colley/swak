package com.swak.common.util;


@FunctionalInterface
public interface BiIntFunction<T, R> {
    R apply(T t, int i);
}