package com.swak.i18n;

import com.swak.common.util.GetterUtil;
import com.swak.common.util.StringPool;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class I18nLocaleContext {


    public static void setLocale(@Nullable Locale locale) {
        setLocale(locale, true);
    }

    public static void setLocale(@Nullable Locale locale, boolean inheritable) {
        LocaleContextHolder.setLocale(locale, inheritable);
    }

    public static void setDefaultLocale(@Nullable Locale locale) {
        LocaleContextHolder.setDefaultLocale(locale);
    }

    public static void setLocale(@Nullable String localeStr) {
        Locale locale = getLocale(localeStr);
        Optional.ofNullable(locale).ifPresent(I18nLocaleContext::setLocale);
    }

    public static void setLocale(@Nullable String localeStr, boolean inheritable) {
        Locale locale = getLocale(localeStr);
        Optional.ofNullable(locale).ifPresent(l -> setLocale(l, inheritable));
    }

    public static void setDefaultLocale(@Nullable String localeStr) {
        Locale locale = getLocale(localeStr);
        Optional.ofNullable(locale).ifPresent(I18nLocaleContext::setDefaultLocale);
    }

    public static Locale getLocale(String locale) {
        if (StringUtils.isEmpty(locale)) {
            return null;
        }
        String[] localeArr;
        if (locale.contains(StringPool.UNDERSCORE)) {
            localeArr = GetterUtil.getSplitStr(locale, StringPool.UNDERSCORE);
        } else {
            localeArr = GetterUtil.getSplitStr(locale, StringPool.DASH);
        }
        if (ArrayUtils.isEmpty(localeArr)) {
            return null;
        }
        int len = localeArr.length;
        return new Locale(localeArr[0],
                len > 1 ? localeArr[1] : StringPool.EMPTY,
                len > 2 ? localeArr[2] : StringPool.EMPTY);
    }

    public static Locale parseLocaleValue(HttpServletRequest request, String paramName) {
        String newLocale = getLocaleLanguage(request,paramName);
        if(StringUtils.isNotEmpty(newLocale)){
            return getLocale(newLocale);
        }
        return null;
    }

    public static String getLocaleLanguage(HttpServletRequest request, String paramName) {
        String newLocale =  getLocale(paramName,request::getHeader);
        if (StringUtils.isNotEmpty(newLocale)) {
            return newLocale;
        }
        newLocale = getLocale(paramName,request::getParameter);
        if (StringUtils.isNotEmpty(newLocale)) {
            return newLocale;
        }
        return getLocaleByCookie(request, paramName);
    }


    public static String getLocale(String paramName, Function<String, String> function) {
        String newLocale = function.apply(paramName);
        if(Objects.nonNull(newLocale)){
            return newLocale;
        }
        String[] paramNames = new String[]{StringUtils.capitalize(paramName),
                StringUtils.uncapitalize(paramName), paramName.toLowerCase(),paramName.toUpperCase()};
        for (String newName : paramNames) {
            newLocale = function.apply(newName);
            if(Objects.nonNull(newLocale)){
                return newLocale;
            }
        }
        return null;
    }

    public static String getLocaleByCookie(HttpServletRequest request, String paramName) {
        Cookie cookie = WebUtils.getCookie(request, paramName);
        if (Objects.nonNull(cookie)) {
            return cookie.getValue();
        }
        return null;
    }
}
