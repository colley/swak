package com.swak.test.core.environment;

import com.swak.core.environment.SystemEnvironmentConfigurable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.util.Locale;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * {@link SystemEnvironmentConfigurable} 的属性读取与 profile 解析。
 * <p>
 * setEnvironment 会往属性源最前面插一个 SWAK CompositePropertySource（banner-mode=off），
 * 这里用 spring-test 的 {@link MockEnvironment} 验证；非 ConfigurableEnvironment 的实现走兜底分支。
 */
@DisplayName("系统环境配置")
class SystemEnvironmentConfigurableTest {

    @Test
    @DisplayName("构造器：单包 / 包+初始化开关 / 可变参数包")
    void constructors() {
        SystemEnvironmentConfigurable single = new SystemEnvironmentConfigurable("com.swak");
        assertThat(single.getBasePackages()).containsExactly("com.swak");
        assertThat(single.getInitializeLocalType()).isFalse();

        SystemEnvironmentConfigurable withSwitch =
                new SystemEnvironmentConfigurable("com.swak", true);
        assertThat(withSwitch.getInitializeLocalType()).isTrue();

        SystemEnvironmentConfigurable multi =
                new SystemEnvironmentConfigurable(false, "com.a", "com.b");
        assertThat(multi.getBasePackages()).containsExactly("com.a", "com.b");
        assertThat(multi.getInitializeLocalType()).isFalse();
    }

    @Test
    @DisplayName("普通 setter：i18n、locale、时区、headerUserKey 等默认值")
    void settersAndDefaults() {
        SystemEnvironmentConfigurable config = new SystemEnvironmentConfigurable("com.swak");

        assertThat(config.getLocaleParamName()).isEqualTo("Lang");
        assertThat(config.getDefaultLocale()).isEqualTo(Locale.getDefault());
        assertThat(config.getHeaderUserKey()).isEqualTo("userId");
        assertThat(config.getI18nDictNames()).isNull();
        assertThat(config.getI18nBaseNames()).isNull();

        config.setBasePackage("com.x");
        config.setI18nBaseNames("i18n/messages");
        config.setI18nDictNames(new String[]{"dict"});
        config.setDefaultLocale(Locale.CHINA);
        config.setDefaultTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        config.setLocaleParamName("lang");
        config.setInitializeLocalType(true);
        config.setSystemAppCode("APP");
        config.setTraceIdPre("trace-");
        config.setHeaderUserKey("uid");

        assertThat(config.getBasePackages()).containsExactly("com.x");
        assertThat(config.getI18nBaseNames()).containsExactly("i18n/messages");
        assertThat(config.getI18nDictNames()).containsExactly("dict");
        assertThat(config.getDefaultLocale()).isEqualTo(Locale.CHINA);
        assertThat(config.getDefaultTimeZone().getID()).isEqualTo("Asia/Shanghai");
        assertThat(config.getLocaleParamName()).isEqualTo("lang");
        assertThat(config.getInitializeLocalType()).isTrue();
        assertThat(config.getSystemAppCode()).isEqualTo("APP");
        assertThat(config.getTraceIdPre()).isEqualTo("trace-");
        assertThat(config.getHeaderUserKey()).isEqualTo("uid");
    }

    @Test
    @DisplayName("setEnvironment：插入 banner-mode=off 的属性源")
    void setEnvironmentAddsSwakPropertySource() {
        MockEnvironment environment = new MockEnvironment();
        SystemEnvironmentConfigurable config = new SystemEnvironmentConfigurable("com.swak");

        config.setEnvironment(environment);

        assertThat(config.getEnvironment()).isSameAs(environment);
        assertThat(environment.getProperty("spring.main.banner-mode")).isEqualTo("off");
    }

    @Test
    @DisplayName("afterPropertiesSet：读取应用名并按 activeProfile 判定环境")
    void afterPropertiesSetWithTestProfile() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(SystemEnvironmentConfigurable.SYS_APP_NAME, "swak-demo");
        environment.setActiveProfiles("test");

        SystemEnvironmentConfigurable config = new SystemEnvironmentConfigurable("com.swak");
        config.setEnvironment(environment);
        config.afterPropertiesSet();

        assertThat(config.getAppName()).isEqualTo("swak-demo");
        assertThat(config.isTestEnv()).isTrue();
        assertThat(config.getCurrentEnv()).isEqualTo("test");
        assertThat(config.isUatEnv()).isFalse();
        assertThat(config.isLocal()).isFalse();
        assertThat(config.isProductEnv()).isFalse();
    }

    @Test
    @DisplayName("afterPropertiesSet：prod profile 判为生产环境，未设置 profile 时为 unknown")
    void afterPropertiesSetWithOtherProfiles() throws Exception {
        MockEnvironment prod = new MockEnvironment();
        prod.setProperty(SystemEnvironmentConfigurable.SYS_APP_NAME, "swak-prod");
        prod.setActiveProfiles("prod");
        SystemEnvironmentConfigurable prodConfig = new SystemEnvironmentConfigurable("com.swak");
        prodConfig.setEnvironment(prod);
        prodConfig.afterPropertiesSet();

        assertThat(prodConfig.isProductEnv()).isTrue();
        assertThat(prodConfig.isTestEnv()).isFalse();

        MockEnvironment none = new MockEnvironment();
        SystemEnvironmentConfigurable defaultConfig = new SystemEnvironmentConfigurable("com.swak");
        defaultConfig.setEnvironment(none);
        defaultConfig.afterPropertiesSet();

        assertThat(defaultConfig.getCurrentEnv()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("已知缺陷：不认识的 activeProfile 会让 sysEnv 变成 null，getCurrentEnv 直接 NPE")
    void unknownProfileLeavesNullEnv() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("staging");

        SystemEnvironmentConfigurable config = new SystemEnvironmentConfigurable("com.swak");
        config.setEnvironment(environment);
        config.afterPropertiesSet();

        assertThat(config.isTestEnv()).isFalse();
        assertThatThrownBy(config::getCurrentEnv).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("getProperty / getString / getBoolean / getInteger / getLong：存在值与默认值")
    void propertyAccessors() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(SystemEnvironmentConfigurable.SYS_APP_NAME, "swak-demo");
        environment.setProperty("swak.flag.name", "swak");
        environment.setProperty("swak.flag.enabled", "true");
        environment.setProperty("swak.count", "42");
        environment.setProperty("swak.big", "9999999999");

        SystemEnvironmentConfigurable config = new SystemEnvironmentConfigurable("com.swak");
        config.setEnvironment(environment);

        assertThat(config.getProperty("swak.flag.name", String.class, "def")).isEqualTo("swak");
        assertThat(config.getProperty("missing", String.class, "def")).isEqualTo("def");

        assertThat(config.getString("swak.flag.name")).isEqualTo("swak");
        assertThat(config.getString("missing")).isNull();
        assertThat(config.getString("missing", "def")).isEqualTo("def");

        assertThat(config.getBoolean("swak.flag.enabled", false)).isTrue();
        assertThat(config.getBoolean("missing", true)).isTrue();

        assertThat(config.getInteger("swak.count", 0)).isEqualTo(42);
        assertThat(config.getInteger("missing", 7)).isEqualTo(7);
        assertThat(config.getInteger("missing")).isNull();

        assertThat(config.getLong("swak.big", 0L)).isEqualTo(9999999999L);
        assertThat(config.getLong("missing", 5L)).isEqualTo(5L);
        assertThat(config.getLong("missing")).isNull();
    }

    @Test
    @DisplayName("getAppName：显式 setAppName 优先于环境变量")
    void getAppNamePrefersExplicitValue() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(SystemEnvironmentConfigurable.SYS_APP_NAME, "from-env");
        SystemEnvironmentConfigurable config = new SystemEnvironmentConfigurable("com.swak");
        config.setEnvironment(environment);

        assertThat(config.getAppName()).isEqualTo("from-env");
        config.setAppName("explicit");
        assertThat(config.getAppName()).isEqualTo("explicit");
    }

    @Test
    @DisplayName("非 ConfigurableEnvironment：loadSharedConfiguration 走 StandardEnvironment 兜底，不抛异常")
    void nonConfigurableEnvironmentFallsBack() {
        Environment environment = mock(Environment.class);
        SystemEnvironmentConfigurable config = new SystemEnvironmentConfigurable("com.swak");

        config.setEnvironment(environment);

        assertThat(config.getEnvironment()).isSameAs(environment);
    }
}
