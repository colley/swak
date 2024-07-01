/**
 * Copyright © 2022 SWAK Info.
 * File Name: DefaultConfigPipeline.java
 */
package com.swak.core.support;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author colley.ma
 * @since  2022/03/04
 */
public class DefaultConfigPipeline<T> implements ConfigPipeline<T> {

    /**
     * NULL_EMPTY
     */
    private final List<T> NULL_EMPTY = Collections.emptyList();
    private LinkedList<T> pipeline = Lists.newLinkedList();
    /**
     * init pipelineList for NULL_EMPTY
     */
    private List<T> pipelineList = NULL_EMPTY;

    private DefaultConfigPipeline() {
    }

    public static <T> ConfigPipeline<T> configPipeline() {
        return new DefaultConfigPipeline<>();
    }

    @Override
    public T getFirst() {
        if (!isEmpty()) {
            return pipeline.getFirst();
        }
        return null;
    }

    @Override
    public T getLast() {
        if (!isEmpty()) {
            return pipeline.getFirst();
        }
        return null;
    }

    @Override
    public ConfigPipeline<T> addFirst(T handler) {
        if (handler != null) {
            pipeline.addFirst(handler);
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        return pipeline.isEmpty();
    }

    @Override
    public ConfigPipeline<T> addLast(T handler) {
        if (handler != null) {
            pipeline.addLast(handler);
        }
        return this;
    }

    @Override
    public ConfigPipeline<T> addAfter(int index, T handler) {
        if (handler != null) {
            pipeline.add(index + 1, handler);
        }
        return this;
    }

    @Override
    public ConfigPipeline<T> addBefore(int index, T handler) {
        if (handler != null) {
            pipeline.add(index, handler);
        }
        return this;
    }

    @Override
    public ConfigPipeline<T> replace(int index, T handler) {
        if (handler != null) {
            pipeline.remove(index);
            pipeline.add(index, handler);
        }
        return this;
    }

    @Override
    public ConfigPipeline<T> removeFirst() {
        if (!pipeline.isEmpty()) {
            pipeline.removeFirst();
        }
        return this;
    }

    @Override
    public ConfigPipeline<T> removeLast() {
        if (!pipeline.isEmpty()) {
            pipeline.removeLast();
        }
        return this;
    }

    @Override
    public ConfigPipeline<T> remove(int index) {
        pipeline.remove(index);
        return this;
    }

    @Override
    public List<T> pipelineList() {
        if (pipelineList == NULL_EMPTY) {
            pipelineList = Collections.unmodifiableList(pipeline);
        }
        return pipelineList;
    }
}
