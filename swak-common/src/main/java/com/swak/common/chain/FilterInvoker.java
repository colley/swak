package com.swak.common.chain;

/**
 * @author colley.ma
 * @since 2.4.0
 */
public interface FilterInvoker<T> {

    default public void invoke(T context){};
}
