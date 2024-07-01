package com.swak.core.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class RestTemplateBuilder {

    private int readTimeout = 5000;

    private int writeTimeout = 5000;

    private int connectTimeout = 5000;

    public static SwakRestTemplate restTemplate() {
        return RestTemplateBuilder.newBuilder().setConnectTimeout(1200).setReadTimeout(1200)
                .setWriteTimeout(1200).build();
    }

    public static SwakRestTemplate restTemplate(int readTimeout, int writeTimeout) {
        return RestTemplateBuilder.newBuilder().setConnectTimeout(1200).setReadTimeout(readTimeout)
                .setWriteTimeout(writeTimeout).build();
    }

    public static RestTemplateBuilder newBuilder() {
        return new RestTemplateBuilder();
    }


    /**
     * Sets the underlying read timeout in milliseconds.
     * A value of 0 specifies an infinite timeout.
     */
    public RestTemplateBuilder setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the underlying write timeout in milliseconds.
     * A value of 0 specifies an infinite timeout.
     */
    public RestTemplateBuilder setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Sets the underlying connect timeout in milliseconds.
     * A value of 0 specifies an infinite timeout.
     */
    public RestTemplateBuilder setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public SwakRestTemplate build() {
        OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory();
        requestFactory.setConnectTimeout(this.connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        requestFactory.setWriteTimeout(writeTimeout);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        extendMessageConverters(restTemplate.getMessageConverters());
        return new SwakRestTemplate(restTemplate);
    }

    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        SimpleModule simpleModule = new SimpleModule();
        jackson2HttpMessageConverter.getObjectMapper().registerModule(simpleModule);
        jackson2HttpMessageConverter.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jackson2HttpMessageConverter.getObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        jackson2HttpMessageConverter.getObjectMapper().setTimeZone(TimeZone.getTimeZone("GMT+8"));

        Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
        if (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            // 原有的String是ISO-8859-1编码 去掉
            if (converter instanceof StringHttpMessageConverter) {
                iterator.remove();
            }
        }
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        converters.add(0, jackson2HttpMessageConverter);
    }
}
