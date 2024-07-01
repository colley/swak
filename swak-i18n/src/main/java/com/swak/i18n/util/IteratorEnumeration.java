package com.swak.i18n.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class IteratorEnumeration<T> implements Enumeration<T> {

    private final Iterator<T> source;

    /**
     * Creates a new IterationEnumeration.
     *
     * @param source The source iterator. Must not be null.
     */
    public IteratorEnumeration(Iterator<T> source) {
        if (source == null) {
            throw new IllegalArgumentException("Source must not be null");
        }
        this.source = source;
    }

    @Override
    public boolean hasMoreElements() {
        return source.hasNext();
    }

    @Override
    public T nextElement() {
        return source.next();
    }
}
