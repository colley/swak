package com.swak.excel;

@FunctionalInterface
public interface QueryDataHandler<T> {
    T query();
}
