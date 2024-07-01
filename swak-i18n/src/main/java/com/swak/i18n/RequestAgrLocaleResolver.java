package com.swak.i18n;

import com.swak.core.web.SwakWebUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.i18n.AbstractLocaleContextResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

/**
 * @author colley.ma
 * @since 3.0.0
 */

@Slf4j
public class RequestAgrLocaleResolver extends AbstractLocaleContextResolver {


    public static final String LOCALE_REQ_ATTRIBUTE_NAME = RequestAgrLocaleResolver.class.getName() + ".LOCALE";

    public static final String TIME_ZONE_REQ_ATTRIBUTE_NAME = RequestAgrLocaleResolver.class.getName() + ".TIME_ZONE";

    public static final String LANG_REQ_ATTRIBUTE_NAME = RequestAgrLocaleResolver.class.getName() + ".LANG";


    private String localeAttributeName = LOCALE_REQ_ATTRIBUTE_NAME;

    private String timeZoneAttributeName = TIME_ZONE_REQ_ATTRIBUTE_NAME;


    @Getter
    private String langAttributeName = LANG_REQ_ATTRIBUTE_NAME;

    @Setter
    @Getter
    private String paramName = "lang";


    public void setLocaleAttributeName(String localeAttributeName) {
        this.localeAttributeName = localeAttributeName;
    }

    public void setLangAttributeName(String localeAttributeName) {
        this.langAttributeName = langAttributeName;
    }

    public void setTimeZoneAttributeName(String timeZoneAttributeName) {
        this.timeZoneAttributeName = timeZoneAttributeName;
    }


    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        parseLocaleIfNecessary(request);
        Locale locale = (Locale) SwakWebUtils.getAttribute(request, this.localeAttributeName);
        if (locale == null) {
            locale = determineDefaultLocale(request);
        }
        return locale;
    }

    @Override
    public LocaleContext resolveLocaleContext(final HttpServletRequest request) {
        return new TimeZoneAwareLocaleContext() {
            @Override
            public Locale getLocale() {
                Locale locale = (Locale) SwakWebUtils.getAttribute(request, localeAttributeName);
                if (locale == null) {
                    locale = determineDefaultLocale(request);
                }
                return locale;
            }

            @Override
            @Nullable
            public TimeZone getTimeZone() {
                TimeZone timeZone = (TimeZone) SwakWebUtils.getAttribute(request, timeZoneAttributeName);
                if (timeZone == null) {
                    timeZone = determineDefaultTimeZone(request);
                }
                return timeZone;
            }
        };
    }

    @Override
    public void setLocaleContext(HttpServletRequest request, @Nullable HttpServletResponse response,
                                 @Nullable LocaleContext localeContext) {
        Locale locale = null;
        TimeZone timeZone = null;
        if (localeContext != null) {
            locale = localeContext.getLocale();
            if (localeContext instanceof TimeZoneAwareLocaleContext) {
                timeZone = ((TimeZoneAwareLocaleContext) localeContext).getTimeZone();
            }
        }
        SwakWebUtils.setAttribute(request, this.localeAttributeName, locale);
        SwakWebUtils.setAttribute(request, this.timeZoneAttributeName, timeZone);
        SwakWebUtils.setAttribute(request, langAttributeName, Optional.ofNullable(locale).map(Locale::toLanguageTag).orElse(null));
        LocaleContextHolder.setLocaleContext(localeContext, true);
    }


    protected Locale determineDefaultLocale(HttpServletRequest request) {
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale == null) {
            defaultLocale = request.getLocale();
        }
        return defaultLocale;
    }

    @Nullable
    protected TimeZone determineDefaultTimeZone(HttpServletRequest request) {
        return getDefaultTimeZone();
    }

    private void parseLocaleIfNecessary(HttpServletRequest request) {
        if (SwakWebUtils.getAttribute(request, localeAttributeName) == null) {
            String localeLang = I18nLocaleContext.getLocaleLanguage(request, getParamName());
            if (StringUtils.isNotEmpty(localeLang)) {
                Locale locale = I18nLocaleContext.getLocale(localeLang);
                SwakWebUtils.setAttribute(request, localeAttributeName, locale);
                SwakWebUtils.setAttribute(request, langAttributeName, localeLang);
            }
        }
    }
}
