package com.swak.core;

import org.springframework.core.Ordered;

/**
 * swak配置常量定义
 *
 * @author colley.ma
 * @since 2022/07/14
 */
public final class SwakConstants {

    public final static String BASE_PACKAGE = "com.swak";
    public final static String SWAK = "swak";

    public final static String SWAK_SYSTEM = SWAK + ".system";

    public final static String SWAK_THREAD_POOL = SWAK + ".threadpool";

    public final static String SWAK_SERVER = SWAK + ".server";

    public final static String SWAK_SPI = SWAK + ".spi";

    public final static String SWAK_CONFIG = SWAK + ".config";

    public final static String SWAK_RATE_LIMIT = SWAK + ".ratelimit";

    public final static String SWAK_HYSTRIX = SWAK + ".hystrix";

    public final static String SWAK_CACHE = SWAK + ".cache";

    public final static String SWAK_CACHE_REDIS = SWAK_CACHE + ".client-type";

    public final static String SWAK_LOCK = SWAK + ".lock";

    public final static String SWAK_DATA_SOURCE = SWAK + ".datasource";

    public final static String SWAK_ARCHIVE = SWAK + ".archive";

    public final static String SWAK_SWAGGER = SWAK + ".swagger";
    public final static String SWAK_SECURITY = SWAK + ".security";

    public final static String SWAK_ZOOKEEPER = SWAK + ".zookeeper";
    // swak base order
    public final static int ORDER_PRECEDENCE = Ordered.HIGHEST_PRECEDENCE + 50;

    public final static int SPI_PRIORITY = 100;


    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String SWAK_DOCS_URL = "https://colley.github.io";

    public static final String SWAK_BOOT_URL = "https://github.com/colley/swak";


    public static final String DISCUSS_GROUP = "418234751@qq.com";
}
