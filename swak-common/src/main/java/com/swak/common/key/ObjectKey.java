package com.swak.common.key;


import com.swak.common.exception.SwakAssert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class ObjectKey implements Comparable<ObjectKey>, Serializable {

    public static final ObjectKey EMPTY = new ObjectKey(new Object[0]);

    private final Object[] params;

    private transient int hashCode;

    public ObjectKey(Object... elements) {
        SwakAssert.notNull(elements, "Elements must not be null");
        this.params = elements.clone();
        this.hashCode = Arrays.deepHashCode(this.params);
    }

    public <T> T get(int index) {
        if (index >= params.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + params.length);
        }
        return (T) params[index];
    }

    public static ObjectKey asKey(Object... elements) {
        return new ObjectKey(elements);
    }

    public boolean equals(Object other) {
        return this == other || other instanceof ObjectKey &&
                Arrays.deepEquals(this.params, ((ObjectKey) other).params);
    }

    public final int hashCode() {
        return this.hashCode;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [" +
                StringUtils.arrayToCommaDelimitedString(this.params) + "]";
    }

    @Override
    public int compareTo(ObjectKey other) {
        return Arrays.deepToString(params).
                compareTo(Arrays.deepToString(other.params));
    }
}
