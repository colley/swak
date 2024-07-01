
package com.swak.core.support;

import com.google.common.collect.Lists;

import java.util.List;


public interface ConfigPipeline<T> {

    public T getFirst();

    public T getLast();

    public ConfigPipeline<T> addFirst(T handler);

    default public ConfigPipeline<T> addFirst(T[] handlers) {
        if (handlers != null) {
            addFirst(Lists.newArrayList(handlers));
        }
        return this;
    }

    default public ConfigPipeline<T> addFirst(List<T> handlers) {
        for (int i = handlers.size() - 1; i >= 0; i--) {
            addFirst(handlers.get(i));
        }
        return this;
    }

    default public ConfigPipeline<T> addLast(List<T> handlers) {
        for (int i = handlers.size() - 1; i >= 0; i--) {
            addLast(handlers.get(i));
        }
        return this;
    }

    default public ConfigPipeline<T> addLast(T[] handlers) {
        if (handlers != null) {
            addLast(Lists.newArrayList(handlers));
        }
        return this;
    }

    public boolean isEmpty();

    ConfigPipeline<T> addLast(T handler);

    ConfigPipeline<T> addAfter(int index, T handler);

    ConfigPipeline<T> addBefore(int index, T handler);

    ConfigPipeline<T> replace(int index, T handler);

    ConfigPipeline<T> removeFirst();

    ConfigPipeline<T> removeLast();

    ConfigPipeline<T> remove(int index);

    public List<T> pipelineList();

}
