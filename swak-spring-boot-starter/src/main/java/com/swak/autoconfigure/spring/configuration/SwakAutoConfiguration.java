package com.swak.autoconfigure.spring.configuration;

import com.google.common.collect.Lists;
import com.swak.autoconfigure.service.LocalDictionaryService;
import com.swak.autoconfigure.service.impl.LocalDictionaryServiceImpl;
import com.swak.common.i18n.I18nDict;
import com.swak.common.i18n.I18nMessageUtil;
import com.swak.common.i18n.MessageI18nSource;
import com.swak.core.SwakConstants;
import com.swak.core.environment.SystemEnvironmentConfigurable;
import com.swak.core.eventbus.EventBusConfig;
import com.swak.core.eventbus.EventHandlerAdapter;
import com.swak.core.monitor.async.AsyncThreadPoolMonitor;
import com.swak.core.support.SpringBeanFactory;
import com.swak.i18n.DefaultMessageI18nSource;
import com.swak.i18n.I18nSourceProperties;
import com.swak.i18n.LocaleCompositeInterceptor;
import com.swak.i18n.LocaleCompositeResolver;
import com.swak.i18n.aggregate.AggregateResourceBundleMessageSource;
import com.swak.i18n.constants.I18nConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@ComponentScan(basePackages = SwakConstants.BASE_PACKAGE)
@EnableAspectJAutoProxy
@EnableWebMvc
@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
public class SwakAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(SpringBeanFactory.class)
    public SpringBeanFactory springBeanFactory() {
        return new SpringBeanFactory();
    }


    @Bean
    @ConditionalOnMissingBean(LocalDictionaryService.class)
    public LocalDictionaryService LocalDictionaryService() {
        return new LocalDictionaryServiceImpl();
    }


    @Bean
    @ConditionalOnMissingBean(CommonsMultipartResolver.class)
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("UTF-8");
        multipartResolver.setMaxUploadSize(31 * 1024 * 1024);
        multipartResolver.setMaxInMemorySize(40960);
        return multipartResolver;
    }

    @Bean("asyncThreadPoolMonitor")
    public AsyncThreadPoolMonitor asyncThreadPoolMonitor() {
        AsyncThreadPoolMonitor starter = new AsyncThreadPoolMonitor();
        starter.setMonitoringPeriod(180); //180s
        return starter;
    }

    /**
     * 单独的线程池 1,10,100000，超过丢弃
     */
    @Bean
    @ConditionalOnMissingBean(EventHandlerAdapter.class)
    @ConditionalOnBean(EventBusConfig.class)
    public EventHandlerAdapter eventHandlerAdapter(@Autowired(required = false) EventBusConfig eventBusConfig, AsyncThreadPoolMonitor asyncThreadPoolMonitor) {
        ThreadPoolTaskExecutor eventBusPool = new ThreadPoolTaskExecutor();
        eventBusConfig = Optional.ofNullable(eventBusConfig).orElse(EventBusConfig.defaultConf());
        eventBusPool.setCorePoolSize(eventBusConfig.getCorePoolSize());// 当前线程数
        eventBusPool.setMaxPoolSize(eventBusConfig.getMaxPoolSize());// 最大线程数
        eventBusPool.setQueueCapacity(eventBusConfig.getQueueCapacity());
        eventBusPool.setWaitForTasksToCompleteOnShutdown(true);
        eventBusPool.setAwaitTerminationSeconds(60 * 10);// 等待时间 （默认为0，此时立即停止），并没等待xx秒后强制停止
        eventBusPool.setThreadNamePrefix("EventBus-threadPool-");
        eventBusPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        eventBusPool.initialize(); // 初始化
        EventHandlerAdapter eventBusAdapter = new EventHandlerAdapter(eventBusPool);
        if (Objects.nonNull(asyncThreadPoolMonitor)) {
            asyncThreadPoolMonitor.addTaskExecutor(eventBusPool);
        }
        return eventBusAdapter;
    }


    @Bean(name = "swakLocaleResolver")
    public LocaleResolver localeResolver(@Autowired(required = false) SystemEnvironmentConfigurable systemConfigurable) {
        LocaleCompositeResolver localeCompositeResolver = new LocaleCompositeResolver();
        if (Objects.nonNull(systemConfigurable)) {
            localeCompositeResolver.setDefaultLocale(systemConfigurable.getDefaultLocale());
            localeCompositeResolver.setParamName(systemConfigurable.getLocaleParamName());
        }
        return localeCompositeResolver;
    }

    @Bean
    @ConditionalOnMissingBean(name = "localeCompositeInterceptor")
    public LocaleCompositeInterceptor localeCompositeInterceptor(@Autowired(required = false) SystemEnvironmentConfigurable systemConfigurable,
                                                                 @Qualifier(value = "swakLocaleResolver") LocaleResolver swakLocaleResolver) {
        LocaleCompositeInterceptor localeCompositeInterceptor = new LocaleCompositeInterceptor(swakLocaleResolver);
        if (Objects.nonNull(systemConfigurable)) {
            localeCompositeInterceptor.setParamName(systemConfigurable.getLocaleParamName());
        }
        return localeCompositeInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean(name = "swakI18nMessageSource")
    public MessageSource swakI18nMessageSource(@Autowired(required = false) SystemEnvironmentConfigurable systemConfigurable,
                                               @Autowired(required = false) MessageSourceProperties properties) {
        I18nSourceProperties i18nSourceProperties = new I18nSourceProperties();
        i18nSourceProperties.getBaseNames().addAll(Lists.newArrayList(I18nConstants.I18N_RESULT_BASE_NAMES));
        if (Objects.nonNull(systemConfigurable)) {
            if (ArrayUtils.isNotEmpty(systemConfigurable.getI18nBaseNames())) {
                i18nSourceProperties.getBaseNames().addAll(Lists.newArrayList(systemConfigurable.getI18nBaseNames()));
            }
            if (Objects.nonNull(systemConfigurable.getDefaultLocale())) {
                i18nSourceProperties.setDefaultLocale(systemConfigurable.getDefaultLocale());
            }
        }
        if (properties != null) {
            i18nSourceProperties.getBaseNames().addAll(Lists.newArrayList(StringUtils
                    .commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(properties.getBasename()))));
            i18nSourceProperties.setEncoding(properties.getEncoding());
            i18nSourceProperties.setCacheDuration(properties.getCacheDuration());
            i18nSourceProperties.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
            i18nSourceProperties.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
            i18nSourceProperties.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
        }
        return messageSource(i18nSourceProperties);
    }

    private MessageSource messageSource(I18nSourceProperties properties) {
        AggregateResourceBundleMessageSource messageSource = new AggregateResourceBundleMessageSource(true);
        messageSource.setBasenames(properties.getBaseNames().toArray(new String[0]));
        if (properties.getEncoding() != null) {
            messageSource.setDefaultEncoding(properties.getEncoding().name());
        }
        messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
        Duration cacheDuration = properties.getCacheDuration();
        if (cacheDuration != null) {
            messageSource.setCacheMillis(cacheDuration.toMillis());
        }
        messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
        messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
        if (Objects.nonNull(properties.getDefaultLocale())) {
            messageSource.setDefaultLocale(properties.getDefaultLocale());
        }
        return messageSource;
    }

    @Bean(name = "messageI18nSource")
    @ConditionalOnMissingBean(name = "messageI18nSource")
    public MessageI18nSource messageI18nSource(@Qualifier(value = "swakI18nMessageSource")
                                                       MessageSource swakI18nMessageSource) {
        MessageI18nSource messageI18nSource = new DefaultMessageI18nSource(swakI18nMessageSource);
        I18nMessageUtil.setMessageSource(messageI18nSource);
        return messageI18nSource;
    }

    @Bean(name = "i18nDictMessageSource")
    @ConditionalOnMissingBean(name = "i18nDictMessageSource")
    public MessageSource i18nDictMessageSource(@Autowired(required = false) SystemEnvironmentConfigurable systemConfigurable) {
        I18nSourceProperties i18nSourceProperties = new I18nSourceProperties();
        i18nSourceProperties.getBaseNames().addAll(Lists.newArrayList(I18nConstants.I18N_DICT_BASE_NAMES));
        if (Objects.nonNull(systemConfigurable)) {
            if (ArrayUtils.isNotEmpty(systemConfigurable.getI18nDictNames())) {
                i18nSourceProperties.getBaseNames().addAll(Lists.newArrayList(systemConfigurable.getI18nDictNames()));
            }
            if (Objects.nonNull(systemConfigurable.getDefaultLocale())) {
                i18nSourceProperties.setDefaultLocale(systemConfigurable.getDefaultLocale());
            }
        }
        return messageSource(i18nSourceProperties);
    }

    @Bean(name = "messageI18nDictSource")
    @ConditionalOnMissingBean(name = "messageI18nDictSource")
    public MessageI18nSource messageI18nDictSource(@Qualifier(value = "i18nDictMessageSource")
                                                               MessageSource i18nDictMessageSource) {
        MessageI18nSource messageI18nSource = new DefaultMessageI18nSource(i18nDictMessageSource);
        I18nDict.setMessageSource(messageI18nSource);
        return messageI18nSource;
    }
}
