
package com.swak.license.api.io.function;

import java.util.Objects;

/**
 * Like {@link java.util.function.Function}, but may throw any type of exception.
 *
 * @author Christian Schlichtherle
 */
@FunctionalInterface
public interface XFunction<T, R> {

    static <T> XFunction<T, T> identity() {
        return t -> t;
    }

    R apply(T t) throws Exception;

    default <V> XFunction<V, R> compose(XFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return v -> apply(before.apply(v));
    }

    default <V> XFunction<T, V> andThen(XFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return t -> after.apply(apply(t));
    }
}
