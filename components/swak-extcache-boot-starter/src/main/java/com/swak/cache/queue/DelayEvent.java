package com.swak.cache.queue;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * DelayEvent.java
 *
 * @author colley.ma
 * @since 2.4.0
 */

@Data
public class DelayEvent<T> implements Comparable<DelayEvent>, Serializable {

    private T data;

    public DelayEvent() {}

    public DelayEvent(T data) {
        Objects.requireNonNull(data, "data must be not null");
        this.data = data;
    }

    public boolean equals(Object other) {
        return this == other || other instanceof DelayEvent &&
                Objects.deepEquals(data, ((DelayEvent) other).getData());
    }

    public final int hashCode() {
        return this.data != null ? this.data.hashCode() * 29 : 0;
    }

    public String toString() {
        return data.toString();
    }

    @Override
    public int compareTo(DelayEvent other) {
        return Arrays.deepToString(new Object[]{data}).
                compareTo(Arrays.deepToString(new Object[]{other.getData()}));
    }
}
