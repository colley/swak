package com.swak.core.extension;

import java.util.function.Function;

public interface ExtensionExchangeAdapter<T, R> {

    R exchange(Class<T> targetClz, String bizId, String userCase, Function<T, R> exeFunction);
}
