/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: AbstractStateConfigurable.java
 * Encoding: UTF-8
 * Date: 2021年3月26日 下午2:07:11
 * History:
 */
package com.swak.core.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractStateConfigurable<C> extends AbstractConfigurable<C>
    implements StateConfigurable<C> {

    private Map<String, C> config = new ConcurrentHashMap<>();

    protected AbstractStateConfigurable(Class<C> configClass) {
        super(configClass);
    }

    @Override
    public Map<String, C> getConfig() {
        return this.config;
    }
}
