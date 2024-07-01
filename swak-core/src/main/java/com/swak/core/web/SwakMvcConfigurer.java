
package com.swak.core.web;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

public interface SwakMvcConfigurer {

    default void configurePathMatch(PathMatchConfigurer configurer) {
    }


    default void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    }


    default void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    }


    default void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    }

    /**
     * Add {@link Converter Converters} and {@link Formatter Formatters} in addition to the ones
     * registered by default.
     */
    default void addFormatters(FormatterRegistry registry) {
    }

    /**
     * Add Spring MVC lifecycle interceptors for pre- and post-processing of
     * controller method invocations and resource handler requests.
     * Interceptors can be registered to apply to all requests or be limited
     * to a subset of URL patterns.
     */
    default void addInterceptors(InterceptorRegistry registry) {
    }

    /**
     * Add handlers to serve static resources such as images, js, and, css
     * files from specific locations under web application root, the classpath,
     * and others.
     */
    default void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    /**
     * Configure cross origin requests processing.
     * @since 4.2
     */
    default void addCorsMappings(CorsRegistry registry) {
    }

    /**
     * Configure simple automated controllers pre-configured with the response
     * status code and/or a view to render the response body. This is useful in
     * cases where there is no need for custom controller logic -- e.g. render a
     * home page, perform simple site URL redirects, return a 404 status with
     * HTML content, a 204 with no content, and more.
     */
    default void addViewControllers(ViewControllerRegistry registry) {
    }

    /**
     * Configure view resolvers to translate String-based view names returned from
     * controllers into concrete {@link org.springframework.web.servlet.View}
     * implementations to perform rendering with.
     * @since 4.1
     */
    default void configureViewResolvers(ViewResolverRegistry registry) {
    }

    /**
     * Add resolvers to support custom controller method argument types.
     * <p>This does not override the built-in support for resolving handler
     * method arguments. To customize the built-in support for argument
     * resolution, configure {@link RequestMappingHandlerAdapter} directly.
     * @param resolvers initially an empty list
     */
    default void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    }

    /**
     * Add handlers to support custom controller method return value types.
     * <p>Using this option does not override the built-in support for handling
     * return values. To customize the built-in support for handling return
     * values, configure RequestMappingHandlerAdapter directly.
     * @param handlers initially an empty list
     */
    default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
    }

    /**
     * Configure the {@link HttpMessageConverter HttpMessageConverters} to use for reading or writing
     * to the body of the request or response. If no converters are added, a
     * default list of converters is registered.
     * <p><strong>Note</strong> that adding converters to the list, turns off
     * default converter registration. To simply add a converter without impacting
     * default registration, consider using the method
     * {@link #extendMessageConverters(java.util.List)} instead.
     * @param converters initially an empty list of converters
     */
    default void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    /**
     * A hook for extending or modifying the list of converters after it has been
     * configured. This may be useful for example to allow default converters to
     * be registered and then insert a custom converter through this method.
     * @param converters the list of configured converters to extend.
     * @since 4.1.3
     */
    default void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    }


    default void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
    }


    default void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
    }

}
