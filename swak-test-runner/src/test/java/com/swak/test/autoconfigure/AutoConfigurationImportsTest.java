package com.swak.test.autoconfigure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 校验自动配置注册链路本身是否完好。
 * <p>
 * Spring Boot 4 已彻底不支持在 {@code spring.factories} 里注册 EnableAutoConfiguration，
 * 只要 imports 文件写错（包名拼错、类被删、文件放错目录），启动时不会报错，而是该 starter 静默失效。
 * 这里把「注册文件 -> 类名 -> 类可加载」三段串起来断言。
 */
class AutoConfigurationImportsTest {

    private static final ClassLoader CLASS_LOADER = AutoConfigurationImportsTest.class.getClassLoader();

    /**
     * 9 个 imports 文件应注册的全部自动配置类。
     */
    private static final Set<String> EXPECTED_CLASS_NAMES = Set.of(
            // swak-spring-boot-starter
            "com.swak.autoconfigure.spring.configuration.AsyncThreadPoolConfiguration",
            "com.swak.autoconfigure.spring.configuration.SwakAutoConfiguration",
            "com.swak.autoconfigure.spring.configuration.SwakExtensionAutoConfiguration",
            "com.swak.autoconfigure.spring.configuration.SwakDelegatingMvcConfiguration",
            // swak-mybatis
            "com.swak.jdbc.spring.configuration.SwakJdbcAutoConfiguration",
            // components
            "com.swak.archiver.spring.configuration.ArchiverAutoConfiguration",
            "com.swak.lock.spring.configuration.ZookeeperConfiguration",
            "com.swak.lock.spring.configuration.SwakLockAutoConfiguration",
            "com.swak.cache.spring.configuration.SwakCacheAutoConfiguration",
            "com.swak.cache.spring.configuration.DelayedQueueAutoConfiguration",
            "com.swak.easyjob.spring.configuration.EasyJobAutoConfiguration",
            "com.swak.operatelog.spring.configuration.SwakOperatelogAutoConfiguration",
            // swak-license
            "com.swak.license.spring.configuration.SwakLicenseAutoConfiguration",
            "com.swak.license.spi.spring.configuration.SwakLicenseSpiAutoConfiguration"
    );

    @Test
    @DisplayName("AutoConfiguration.imports 注册的 com.swak 自动配置类与预期完全一致")
    void importsDeclareExpectedAutoConfigurations() {
        List<String> actual = ImportCandidates.load(AutoConfiguration.class, CLASS_LOADER)
                .getCandidates()
                .stream()
                .filter(name -> name.startsWith("com.swak."))
                .collect(Collectors.toList());

        assertThat(actual)
                .as("imports 中 com.swak 的自动配置项")
                .hasSize(EXPECTED_CLASS_NAMES.size())
                .containsExactlyInAnyOrderElementsOf(EXPECTED_CLASS_NAMES);
    }

    @Test
    @DisplayName("每个注册的自动配置类都能被类加载器加载，且带 @Configuration（含元注解）")
    void everyDeclaredClassIsLoadableAndConfigured() {
        for (String className : EXPECTED_CLASS_NAMES) {
            Class<?> type = loadClass(className);
            // @AutoConfiguration 自身被 @Configuration(proxyBeanMethods = false) 标注，
            // 所以这里要用元注解查找，不能只看类上的直接注解
            assertThat(AnnotatedElementUtils.hasAnnotation(type, Configuration.class))
                    .as("%s 应标注 @Configuration 或 @AutoConfiguration", className)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("spring.factories 不再残留 EnableAutoConfiguration 注册")
    void springFactoriesHasNoEnableAutoConfiguration() {
        List<String> names = SpringFactoriesLoader
                .loadFactoryNames(org.springframework.boot.autoconfigure.EnableAutoConfiguration.class, CLASS_LOADER);

        assertThat(names)
                .as("Boot 4 会忽略 spring.factories 中的 EnableAutoConfiguration，残留即为无效配置")
                .noneMatch(name -> name.startsWith("com.swak."));
    }

    @Test
    @DisplayName("SpringApplicationRunListener 仍通过 spring.factories 注册且可解析")
    void runListenerIsRegisteredBySpringFactories() {
        List<String> names = SpringFactoriesLoader
                .loadFactoryNames(SpringApplicationRunListener.class, CLASS_LOADER);

        assertThat(names).contains("com.swak.autoconfigure.event.SwakApplicationRunListener");
        assertThat(loadClass("com.swak.autoconfigure.event.SwakApplicationRunListener")).isNotNull();
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, CLASS_LOADER);
        } catch (ClassNotFoundException ex) {
            throw new AssertionError("自动配置类无法加载: " + className, ex);
        }
    }
}
