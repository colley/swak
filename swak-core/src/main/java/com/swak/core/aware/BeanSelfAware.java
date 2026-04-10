/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 20-5-21 下午4:06
 * History:
 */
package com.swak.core.aware;

/**
 * BeanSelfAware.java
 * @author colley.ma
 * @since 2.4.0
 */
public interface BeanSelfAware<T> {

    void setServiceBeanSelf(T serviceBeanSelf);
}
