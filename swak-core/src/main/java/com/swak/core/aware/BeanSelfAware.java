/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 20-5-21 下午4:06
 * History:
 */
package com.swak.core.aware;

/**
 * BeanSelfAware.java
 * 
 * @author ColleyMa
 * @date 20-5-21 下午4:06
 * @version v1.0
 *
 * @param <T> DOCUMENT ME!
 */
public interface BeanSelfAware<T> {

    void setServiceBeanSelf(T serviceBeanSelf);
}
