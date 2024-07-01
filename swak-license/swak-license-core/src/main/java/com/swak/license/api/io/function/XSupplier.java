
package com.swak.license.api.io.function;

/**
 * Like {@link java.util.function.Supplier}, but may throw any type of exception.
 *
 * @author Christian Schlichtherle
 */
@FunctionalInterface
public interface XSupplier<T> {

    T get() throws Exception;
}
