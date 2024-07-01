package com.swak.i18n;

import com.swak.common.i18n.MessageI18nSource;
import com.swak.common.util.StringPool;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;

import java.util.Locale;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class DefaultMessageI18nSource implements MessageI18nSource {

    private MessageSource messageSource;

    public DefaultMessageI18nSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
        return messageSource.getMessage(code, args, locale);
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return messageSource.getMessage(resolvable, locale);
    }

    @Override
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, null, StringPool.EMPTY, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessage(String code, String defaultMessage, Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }
}