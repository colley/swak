/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: StateConfigurable.java
 * Encoding: UTF-8
 * Date: 2021年3月26日 下午2:04:48
 * History:
 */
package com.swak.core.config;

import java.util.Map;

public interface StateConfigurable<C> extends Configurable<C> {

    Map<String, C> getConfig();
}
