package com.swak.i18n;

import com.swak.common.i18n.MessageI18nSource;
import com.swak.common.util.StringPool;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class DefaultMessageI18nSource implements MessageI18nSource {

    private MessageSource messageSource;

    private Locale defaultLocale;

    public DefaultMessageI18nSource(MessageSource messageSource,Locale defaultLocale) {
        this.messageSource = messageSource;
        this.defaultLocale = defaultLocale;
    }

    @Override
    public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
        return messageSource.getMessage(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return messageSource.getMessage(resolvable, locale);
    }

    @Override
    public String getMessage(String code, Object... args) {
         Locale locale = LocaleContextHolder.getLocale();
        if(Objects.isNull(locale) && Objects.nonNull(defaultLocale)){
            I18nLocaleContext.setLocale(defaultLocale);
        }
        return messageSource.getMessage(code, null, StringPool.EMPTY, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(String code, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        if(Objects.isNull(locale) && Objects.nonNull(defaultLocale)){
            I18nLocaleContext.setLocale(defaultLocale);
        }
        return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(String code, String defaultMessage, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        if(Objects.isNull(locale) && Objects.nonNull(defaultLocale)){
            I18nLocaleContext.setLocale(defaultLocale);
        }
        return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }
}