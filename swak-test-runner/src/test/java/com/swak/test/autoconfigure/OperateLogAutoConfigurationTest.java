package com.swak.test.autoconfigure;

import com.swak.operatelog.OperateLogAnnotationParser;
import com.swak.operatelog.OperateLogInterceptor;
import com.swak.operatelog.OperateLogOperationSource;
import com.swak.operatelog.OperateLogService;
import com.swak.operatelog.spring.configuration.SwakOperatelogAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 操作日志切面以业务方提供的 OperateLogService 为前提。
 */
class OperateLogAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SwakOperatelogAutoConfiguration.class));

    @Test
    @DisplayName("没有 OperateLogService 时不装配切面（避免无消费者的空转）")
    void withoutService() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(OperateLogInterceptor.class);
            assertThat(context).doesNotHaveBean(OperateLogAnnotationParser.class);
        });
    }

    @Test
    @DisplayName("存在 OperateLogService 时装配解析器、操作源与拦截器")
    void withService() {
        runner.withBean(OperateLogService.class, () -> mock(OperateLogService.class)).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(OperateLogAnnotationParser.class);
            assertThat(context).hasSingleBean(OperateLogOperationSource.class);
            assertThat(context).hasSingleBean(OperateLogInterceptor.class);
            assertThat(context).hasBean("internalOperateLogAdvisor");
        });
    }
}
