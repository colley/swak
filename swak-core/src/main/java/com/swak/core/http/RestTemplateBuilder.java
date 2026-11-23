package com.swak.core.http;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * RestTemplate 构建器。
 * <p>
 * Spring Framework 7 已移除 OkHttp 支持（{@code OkHttp3ClientHttpRequestFactory} 不存在），
 * 这里改用 JDK 内置的 {@link HttpClient} 作为底层实现，不再引入额外依赖。
 * <p>
 * 注意：JDK HttpClient 只支持连接超时与读取超时，<b>没有写超时概念</b>，
 * 因此 {@link #setWriteTimeout(int)} 仅保留 API 兼容，不再生效。
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class RestTemplateBuilder {

    private int readTimeout = 5000;

    /**
     * 写超时。JDK HttpClient 不支持写超时，该值仅保留 API 兼容，不生效。
     */
    private int writeTimeout = 5000;

    private int connectTimeout = 5000;

    private List<HttpMessageConverter<?>> converters;

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
     *
     * @deprecated JDK HttpClient 不支持写超时，该配置不再生效，仅为 API 兼容保留。
     */
    @Deprecated
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

    public RestTemplateBuilder setHttpMessageConverter(List<HttpMessageConverter<?>> converters) {
        this.converters = converters;
        return this;
    }

    public SwakRestTemplate build() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(this.connectTimeout)).build());
        requestFactory.setReadTimeout(this.readTimeout);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        if(CollectionUtils.isNotEmpty(converters)) {
            restTemplate.setMessageConverters(converters);
        }
        return new SwakRestTemplate(restTemplate);
    }
}
