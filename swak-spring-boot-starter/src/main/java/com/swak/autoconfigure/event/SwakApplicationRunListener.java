package com.swak.autoconfigure.event;

import org.springframework.boot.Banner;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * SwakApplicationRunListener.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class SwakApplicationRunListener implements SpringApplicationRunListener, Ordered {
    private final SpringApplication application;
    private final String[] args;

    public SwakApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        application.setBannerMode(Banner.Mode.OFF);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
