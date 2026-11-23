package com.swak.test.autoconfigure;

import com.swak.archiver.conf.ArchiverProperties;
import com.swak.archiver.executor.ClickArchiverEngineExecutor;
import com.swak.archiver.executor.MysqlArchiverEngineExecutor;
import com.swak.archiver.spring.configuration.ArchiverAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据归档执行器以业务方声明的 ArchiverProperties bean 为前提。
 * <p>
 * 注意：ArchiverProperties 既没有 {@code @ConfigurationProperties} 也没有被
 * {@code @EnableConfigurationProperties} 引入，只能由使用方显式注册为 bean，
 * 因此默认（什么都不配）情况下归档能力是关闭的 —— 这属于有意为之的「按需开启」。
 */
class ArchiverAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ArchiverAutoConfiguration.class));

    @Test
    @DisplayName("未注册 ArchiverProperties 时不装配任何归档执行器")
    void withoutPropertiesBean() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(MysqlArchiverEngineExecutor.class);
            assertThat(context).doesNotHaveBean(ClickArchiverEngineExecutor.class);
        });
    }

    @Test
    @DisplayName("注册 ArchiverProperties 后装配 mysql / clickhouse 两个执行器")
    void withPropertiesBean() {
        runner.withBean(ArchiverProperties.class, ArchiverProperties::new).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(MysqlArchiverEngineExecutor.class);
            assertThat(context).hasSingleBean(ClickArchiverEngineExecutor.class);
        });
    }
}
