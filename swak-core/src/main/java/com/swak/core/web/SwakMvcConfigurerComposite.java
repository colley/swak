package com.swak.core.web;

import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class SwakMvcConfigurerComposite implements SwakMvcConfigurer {

    private final List<SwakMvcConfigurer> delegates = new ArrayList<>();


    public void addWebMvcConfigurers(List<SwakMvcConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.delegates.addAll(configurers);
        }
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.configurePathMatch(configurer);
        }
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.configureContentNegotiation(configurer);
        }
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.configureAsyncSupport(configurer);
        }
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.configureDefaultServletHandling(configurer);
        }
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.addFormatters(registry);
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.addInterceptors(registry);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.addResourceHandlers(registry);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.addCorsMappings(registry);
        }
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.addViewControllers(registry);
        }
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.configureViewResolvers(registry);
        }
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.addArgumentResolvers(argumentResolvers);
        }
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.addReturnValueHandlers(returnValueHandlers);
        }
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.configureMessageConverters(converters);
        }
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.extendMessageConverters(converters);
        }
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.configureHandlerExceptionResolvers(exceptionResolvers);
        }
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        for (SwakMvcConfigurer delegate : this.delegates) {
            delegate.extendHandlerExceptionResolvers(exceptionResolvers);
        }
    }
}
