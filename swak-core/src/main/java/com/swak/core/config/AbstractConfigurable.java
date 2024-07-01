/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: AbstractConfigurable.java
 * Encoding: UTF-8
 * Date: 2021年3月26日 下午2:06:00
 * History:
 */
package com.swak.core.config;

import org.springframework.beans.BeanUtils;

public class AbstractConfigurable<C> implements Configurable<C> {

    private Class<C> configClass;

    protected AbstractConfigurable(Class<C> configClass) {
        this.configClass = configClass;
    }

    @Override
    public Class<C> getConfigClass() {
        return configClass;
    }

    @Override
    public C newConfig() {
        return BeanUtils.instantiateClass(this.configClass);
    }
}
