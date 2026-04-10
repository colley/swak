package com.swak.jdbc.metadata;

import java.io.Serializable;
import java.util.function.Function;

/**
 * SFunction.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
