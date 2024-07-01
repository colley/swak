package com.swak.license.server.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.swak.autoconfigure.config.AsyncProperties;
import com.swak.core.environment.SystemEnvironmentConfigurable;
import com.swak.core.eventbus.EventBusConfig;
import com.swak.core.web.JacksonSerializerFeatureCompatible;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        SimpleModule simpleModule = new SimpleModule();
        ObjectMapper objectMapper =  jackson2HttpMessageConverter.getObjectMapper();
        //simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        //simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        jackson2HttpMessageConverter.getObjectMapper().registerModule(simpleModule);
        jackson2HttpMessageConverter.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jackson2HttpMessageConverter.getObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        jackson2HttpMessageConverter.getObjectMapper().setTimeZone(TimeZone.getTimeZone("GMT+8"));
        BeanSerializerModifier beanSerializerModifier = new JacksonSerializerFeatureCompatible(JacksonSerializerFeatureCompatible.SerializerFeature.WriteNullListAsEmpty);
        jackson2HttpMessageConverter.getObjectMapper().setSerializerFactory(objectMapper.getSerializerFactory().withSerializerModifier(beanSerializerModifier));
        converters.add(0, jackson2HttpMessageConverter);
    }

    /**
     * Event Bus线程配置
     * @return
     */
    @Bean
    public EventBusConfig eventBusConfig() {
        EventBusConfig eventBusConfig = new EventBusConfig();
        eventBusConfig.setCorePoolSize(1);
        eventBusConfig.setMaxPoolSize(2);
        eventBusConfig.setQueueCapacity(100);
        return eventBusConfig;
    }

    @Bean
    public SystemEnvironmentConfigurable systemConfig() {
        SystemEnvironmentConfigurable systemConfig = new SystemEnvironmentConfigurable("com.swak.license.server");
        systemConfig.setInitializeLocalType(true);
        systemConfig.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        systemConfig.setDefaultTimeZone(TimeZone.getTimeZone("GMT+8"));
        return systemConfig;
    }



    /**
     *  DEFAULT_CORE_POOL_SIZE = 10;
     *  DEFAULT_MAX_POOL_SIZE = 60;
     *  DEFAULT_QUEUE_CAPACITY = 1000;
     * @return
     */
    @Bean
    public AsyncProperties asyncProperties() {
        AsyncProperties asyncProperties = new AsyncProperties();
        asyncProperties.setCorePoolSize(2);
        asyncProperties.setMaxPoolSize(5);
        asyncProperties.setQueueCapacity(500);
        return asyncProperties;
    }
}
