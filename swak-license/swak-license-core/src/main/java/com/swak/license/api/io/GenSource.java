
package com.swak.license.api.io;


import com.swak.license.api.io.function.XConsumer;
import com.swak.license.api.io.function.XFunction;

/**
 * A generic abstraction for safe access to some closeable input resource - DO NOT USE THIS DIRECTLY!
 * This type primarily exists to enforce consistency between {@link Source} and {@link ArchiveSource}.
 *
 * @author Christian Schlichtherle
 */
@FunctionalInterface
public interface GenSource<T extends AutoCloseable> {

    /**
     * Returns an input socket for safe access to some closeable input resource.
     */
    Socket<T> input();

    /**
     * Loans a closeable resource from the {@linkplain #input() input socket} to the given consumer.
     * The loaned resource will be closed upon return from this method.
     */
    default void acceptReader(XConsumer<? super T> reader) throws Exception {
        input().accept(reader);
    }

    /**
     * Loans a closeable resource from the {@linkplain #input() input socket} to the given function and returns its
     * value.
     * The loaned resource will be closed upon return from this method.
     * It is an error to return the loaned resource from the given function or any other object which holds on to it.
     */
    default <U> U applyReader(XFunction<? super T, ? extends U> reader) throws Exception {
        return input().apply(reader);
    }
}
