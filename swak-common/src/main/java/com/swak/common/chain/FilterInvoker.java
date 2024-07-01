package com.swak.common.chain;

/**
 * @author colley.ma
 * @date 2018/04/17
 */
public interface FilterInvoker<T> {

    default public void invoke(T context){};
}
