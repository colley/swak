package com.swak.demo.config;


import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.swak.common.util.GetterUtil;
import com.swak.core.environment.SystemEnvironmentConfigurable;
import com.swak.core.eventbus.EventBusConfig;
import com.swak.core.web.JacksonSerializerFeatureCompatible;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@Configuration
public class SystemSwakConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        SimpleModule simpleModule = new SimpleModule();
        ObjectMapper objectMapper = jackson2HttpMessageConverter.getObjectMapper();
        //simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        //simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        jackson2HttpMessageConverter.getObjectMapper().registerModule(simpleModule);
        jackson2HttpMessageConverter.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jackson2HttpMessageConverter.getObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        jackson2HttpMessageConverter.getObjectMapper().setTimeZone(TimeZone.getTimeZone("GMT+8"));
        BeanSerializerModifier beanSerializerModifier = new JacksonSerializerFeatureCompatible(JacksonSerializerFeatureCompatible.SerializerFeature.WriteNullListAsEmpty);
        jackson2HttpMessageConverter.getObjectMapper().setSerializerFactory(objectMapper.getSerializerFactory().withSerializerModifier(beanSerializerModifier));
        converters.add(0, jackson2HttpMessageConverter);
        for (HttpMessageConverter<?> converter : converters) {
            log.warn("HttpMessageConverter of {}", converter.getClass().getName());
        }
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    @Bean
    public EventBusConfig eventBusConfig() {
        EventBusConfig eventBusConfig = new EventBusConfig();
        eventBusConfig.setCorePoolSize(2);
        eventBusConfig.setMaxPoolSize(10);
        eventBusConfig.setQueueCapacity(100);
        return eventBusConfig;
    }

    @Bean
    public SystemEnvironmentConfigurable systemConfig() {
        SystemEnvironmentConfigurable systemConfig = new SystemEnvironmentConfigurable("com.swak.demo");
        systemConfig.setInitializeLocalType(true);
        systemConfig.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        systemConfig.setDefaultTimeZone(TimeZone.getTimeZone("GMT+8"));
        return systemConfig;
    }

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        String[] addresses = GetterUtil.getSplitStr("redis://10.74.170.215:6379,redis://10.74.170.216:6379,redis://10.74.170.217:6379,redis://10.74.170.218:6379,redis://10.74.170.219:6379,redis://10.74.170.220:6379");
        config.useClusterServers().addNodeAddress(addresses);
        return Redisson.create(config);
    }
}