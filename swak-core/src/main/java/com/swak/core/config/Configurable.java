/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: Configurable.java
 * Encoding: UTF-8
 * Date: 2021年3月26日 下午2:02:57
 * History:
 */
package com.swak.core.config;


public interface Configurable<C> {

    Class<C> getConfigClass();

    C newConfig();
}
