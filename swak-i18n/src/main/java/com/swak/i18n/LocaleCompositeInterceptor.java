package com.swak.i18n;

import com.google.common.collect.Lists;
import com.swak.core.web.SwakInterceptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Slf4j
public class LocaleCompositeInterceptor implements AsyncHandlerInterceptor {

    private String[] httpMethods;

    private boolean ignoreInvalidLocale = true;

    @Setter
    @Getter
    private String paramName ="Lang";

    @Setter
    private LocaleResolver localeResolver;

    private final List<SwakInterceptor> delegates = Lists.newArrayList();

    public LocaleCompositeInterceptor(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }


    @Autowired(required = false)
    public void setConfigurers(List<SwakInterceptor> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            delegates.addAll(configurers);
        }
    }

    public void setHttpMethods(@Nullable String... httpMethods) {
        this.httpMethods = httpMethods;
    }

    public String[] getHttpMethods() {
        return this.httpMethods;
    }

    public void setIgnoreInvalidLocale(boolean ignoreInvalidLocale) {
        this.ignoreInvalidLocale = ignoreInvalidLocale;
    }

    public boolean isIgnoreInvalidLocale() {
        return this.ignoreInvalidLocale;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
        String newLocale = I18nLocaleContext.getLocaleLanguage(request,getParamName());
        if (StringUtils.isNotEmpty(newLocale)) {
            request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
            if (checkHttpMethod(request.getMethod())) {
                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                if (localeResolver == null) {
                    throw new IllegalStateException(
                            "No LocaleResolver found: not in a DispatcherServlet request?");
                }
                try {
                    localeResolver.setLocale(request, response, I18nLocaleContext.getLocale(newLocale));
                }
                catch (IllegalArgumentException ex) {
                    if (isIgnoreInvalidLocale()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Ignoring invalid locale value [" + newLocale + "]: " + ex.getMessage());
                        }
                    } else {
                        throw ex;
                    }
                }
            }
            for (SwakInterceptor delegate : delegates) {
                delegate.preHandle(request, response, newLocale);
            }
        }
        // Proceed in any case.
        return true;
    }

    private boolean checkHttpMethod(String currentMethod) {
        String[] configuredMethods = getHttpMethods();
        if (ObjectUtils.isEmpty(configuredMethods)) {
            return true;
        }
        for (String configuredMethod : configuredMethods) {
            if (configuredMethod.equalsIgnoreCase(currentMethod)) {
                return true;
            }
        }
        return false;
    }


}