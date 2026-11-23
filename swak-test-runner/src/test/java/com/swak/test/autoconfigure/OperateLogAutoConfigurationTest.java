package com.swak.test.autoconfigure;

import com.swak.operatelog.OperateLogAnnotationParser;
import com.swak.operatelog.OperateLogInterceptor;
import com.swak.operatelog.OperateLogOperationSource;
import com.swak.operatelog.OperateLogService;
import com.swak.operatelog.spring.configuration.SwakOperatelogAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    @DisplayName("业务方 service 在单例就绪后惰性注入，@OperateLog 方法被代理并走到开关判断")
    void lazyServiceInjectionAndAop() {
        OperateLogService service = mock(OperateLogService.class);
        when(service.isOperateLogEnabled()).thenReturn(false);
        runner.withBean(OperateLogService.class, () -> service)
                .withUserConfiguration(LoggedServiceConfig.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    LoggedService loggedService = context.getBean(LoggedService.class);
                    assertThat(AopUtils.isAopProxy(loggedService)).isTrue();

                    assertThat(loggedService.doWork()).isEqualTo("ok");

                    // 说明拦截器确实拿到了 service（惰性注入成功）
                    verify(service, atLeastOnce()).isOperateLogEnabled();
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class LoggedServiceConfig {
        @Bean
        LoggedService loggedService() {
            return new LoggedService();
        }
    }

    static class LoggedService {
        @com.swak.operatelog.annotation.OperateLog(module = "test", operateType = "query")
        public String doWork() {
            return "ok";
        }
    }
}
