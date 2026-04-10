package com.swak.i18n;

import com.swak.core.web.SwakWebUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

/**
 * @author colley.ma
 * @since 3.0.0
 */

@Slf4j
public class LocaleCompositeResolver implements LocaleContextResolver {

    public static final String LOCALE_REQUEST_ATTRIBUTE_NAME = LocaleCompositeResolver.class.getName() + ".LOCALE";

    public static final String LANG_REQUEST_ATTRIBUTE_NAME = LocaleCompositeResolver.class.getName() + ".LANG";

    @Setter
    @Getter
    private String paramName ="lang";

    private Locale defaultLocale;

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        setLocaleContext(request, response, (locale != null ? new SimpleLocaleContext(locale) : null));
    }

    protected Locale determineDefaultLocale(HttpServletRequest request) {
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale == null) {
            defaultLocale = request.getLocale();
        }
        return defaultLocale;
    }

    /**
     * Set a fixed locale that this resolver will return if no cookie is found.
     */
    public void setDefaultLocale(@Nullable Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Return the fixed locale that this resolver will return if no cookie is found,
     * if any.
     */
    @Nullable
    protected Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        parseLocaleIfNecessary(request);
        Locale locale = (Locale) SwakWebUtils.getRequestAttribute(request,LOCALE_REQUEST_ATTRIBUTE_NAME);
        if (locale == null) {
            locale = determineDefaultLocale(request);
        }
        return locale;
    }

    @Override
    public LocaleContext resolveLocaleContext(final HttpServletRequest request) {
        parseLocaleIfNecessary(request);
        Locale locale = (Locale) SwakWebUtils.getRequestAttribute(request,LOCALE_REQUEST_ATTRIBUTE_NAME);
        if (locale == null) {
            locale = determineDefaultLocale(request);
        }
        return new SimpleLocaleContext(locale);
    }

    @Override
    public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
        Assert.notNull(response, "[Swak-I18n] HttpServletResponse is required for LocaleCompositeResolver");
        if (localeContext != null) {
            request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, localeContext.getLocale());
            request.setAttribute(LANG_REQUEST_ATTRIBUTE_NAME, Optional.ofNullable(localeContext.getLocale()).map(Locale::toLanguageTag).orElse(null));
            LocaleContextHolder.setLocaleContext(localeContext, true);
        }
    }

    private void parseLocaleIfNecessary(HttpServletRequest request) {
        if (request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) == null) {
            String localeLang = I18nLocaleContext.getLocaleLanguage(request, getParamName());
            if (StringUtils.isNotEmpty(localeLang)) {
                Locale locale = I18nLocaleContext.getLocale(localeLang);
                request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME,locale);
                request.setAttribute(LANG_REQUEST_ATTRIBUTE_NAME, localeLang);
            }
        }
    }

}
