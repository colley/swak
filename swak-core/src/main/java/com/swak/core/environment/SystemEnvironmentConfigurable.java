package com.swak.core.environment;

import com.swak.common.util.GetterUtil;
import com.swak.core.support.PropertiesFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.*;

import java.util.Locale;
import java.util.TimeZone;

/**
 * SystemConfigurable ClassName: SystemEnvironmentConfigurable.java
 *
 * @author colley.ma
 * @since 2021年3月24日 下午12:03:26
 */
@Order(-1)
public class SystemEnvironmentConfigurable implements EnvironmentAware, InitializingBean {

    public static final String SYS_APP_NAME = "spring.application.name";
    /**
     * The Environment.
     */
    protected transient Environment environment;
    private String appName;
    private SysEnv sysEnv = SysEnv.UNKNOWN;
    private String[] basePackages;

    private String[] i18nBaseNames;

    @Getter
    @Setter
    private String[] i18nDictNames;

    @Getter
    @Setter
    private Locale defaultLocale = Locale.getDefault();

    @Getter
    @Setter
    private TimeZone defaultTimeZone;

    @Getter
    @Setter
    private String localeParamName = "Lang";
    /**
     * 是否初始化本地枚举
     */
    private Boolean initializeLocalType = false;

    public SystemEnvironmentConfigurable(String basePackage) {
        this.basePackages = new String[]{basePackage};
    }

    public SystemEnvironmentConfigurable(String basePackage, boolean initializeLocalType) {
        this(basePackage);
        this.initializeLocalType = initializeLocalType;
    }

    public SystemEnvironmentConfigurable(boolean initializeLocalType, String... basePackages) {
        this.basePackages = basePackages;
        this.initializeLocalType = initializeLocalType;
    }

    public boolean isTestEnv() {
        return SysEnv.TEST.equals(sysEnv);
    }

    public String getCurrentEnv() {
        return sysEnv.env;
    }

    public boolean isUatEnv() {
        return SysEnv.UAT.equals(sysEnv);
    }

    public boolean isLocal() {
        return SysEnv.LOCAL.equals(sysEnv);
    }

    public boolean isProductEnv() {
        return SysEnv.PROD.equals(sysEnv);
    }

    public String getAppName() {
        return GetterUtil.getString(this.appName, getString(SYS_APP_NAME));
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.appName = getString(SYS_APP_NAME);
        String[] activeProfiles = environment.getActiveProfiles();
        if (ArrayUtils.isNotEmpty(activeProfiles)) {
            this.sysEnv = SysEnv.getEnv(activeProfiles[0]);
        }
    }

    /**
     * Gets property.
     *
     * @param <T>          the type parameter
     * @param key          the key
     * @param targetType   the target type
     * @param defaultValue the default value
     * @return the property
     */
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return environment.getProperty(key, targetType, defaultValue);
    }

    /**
     * Gets string.
     *
     * @param key        the key
     * @param defaultVal the default val
     * @return the string
     */
    public String getString(String key, String defaultVal) {
        return environment.getProperty(key, String.class,
                PropertiesFactory.getString(key, defaultVal));
    }

    /**
     * Gets string.
     *
     * @param key the key
     * @return the string
     */
    public String getString(String key) {
        return environment.getProperty(key, String.class, PropertiesFactory.getString(key));
    }

    /**
     * Gets boolean.
     *
     * @param key        the key
     * @param defaultVal the default val
     * @return the boolean
     */
    public boolean getBoolean(String key, boolean defaultVal) {
        return environment.getProperty(key, Boolean.class,
                PropertiesFactory.getBoolean(key, defaultVal));
    }

    /**
     * Gets integer.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the integer
     */
    public Integer getInteger(String key, int defaultValue) {
        return environment.getProperty(key, Integer.class,
                PropertiesFactory.getInt(key, defaultValue));
    }

    /**
     * Gets integer.
     *
     * @param key the key
     * @return the integer
     */
    public Integer getInteger(String key) {
        return environment.getProperty(key, Integer.class, PropertiesFactory.getInt(key));
    }

    /**
     * Gets long.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the long
     */
    public Long getLong(String key, long defaultValue) {
        return environment.getProperty(key, Long.class,
                PropertiesFactory.getLong(key, defaultValue));
    }

    /**
     * Gets long.
     *
     * @param key the key
     * @return the long
     */
    public Long getLong(String key) {
        return environment.getProperty(key, Long.class, PropertiesFactory.getLong(key));
    }

    /**
     * Gets environment.
     *
     * @return the environment
     */
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        loadSharedConfiguration(environment);
    }

    public String[] getBasePackages() {
        return basePackages;
    }

    public void setBasePackage(String... basePackages) {
        this.basePackages = basePackages;
    }

    public void setI18nBaseNames(String... i18nBaseNames) {
        this.i18nBaseNames = i18nBaseNames;
    }

    public String[] getI18nBaseNames() {
        return i18nBaseNames;
    }

    public Boolean getInitializeLocalType() {
        return initializeLocalType;
    }

    public void setInitializeLocalType(Boolean initializeLocalType) {
        this.initializeLocalType = initializeLocalType;
    }

    private MutablePropertySources getPropertySources(Environment environment) {
        return environment instanceof ConfigurableEnvironment ?
                ((ConfigurableEnvironment) environment).getPropertySources() : (new StandardEnvironment()).getPropertySources();
    }

    public void loadSharedConfiguration(Environment environment) {
        CompositePropertySource composite = new CompositePropertySource("SWAK");
        SwakEnvPropertySource swakEnvPropertySource = new SwakEnvPropertySource();
        swakEnvPropertySource.put("spring.main.banner-mode", "off");
        composite.addFirstPropertySource(swakEnvPropertySource);
        getPropertySources(environment).addFirst(composite);
    }
}
