/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: SysEnv.java
 * Encoding: UTF-8
 * Date: 2021年3月31日 下午2:29:51
 * History:
 */
package com.swak.core.environment;

import java.util.Objects;

public enum SysEnv {

    LOCAL("local", "local environment"),
    DEV("dev", "dev environment"),

    TEST("test", "test environment"),
    UAT("uat", "uat environment"),
    PROD("prod", "prod environment"),

    UNKNOWN("unknown", "unknown"),

    ;

    public String env;

    public String name;

    SysEnv(String env, String name) {
        this.env = env;
        this.name = name;
    }

    public static SysEnv getEnv(String env) {
        for (SysEnv sysEnv : SysEnv.values()) {
            if (Objects.equals(sysEnv.env, env)) {
                return sysEnv;
            }
        }
        return null;
    }

    public boolean eq(String sysEnv) {
        return Objects.equals(env, sysEnv);
    }
}
