package com.swak.common.i18n;

import com.swak.common.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * @author colley.ma
 * @since 3.0.0
 **/
@Slf4j
public class I18nMessageUtil {

    private static MessageI18nSource messageSource;

    public static void setMessageSource(MessageI18nSource source) {
        messageSource = source;
    }

    public static String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
        if (Objects.isNull(messageSource)) {
            log.debug("MessageSource not config,please config i18n messageSource");
            return StringPool.EMPTY;
        }
        return messageSource.getMessage(code, args, locale);
    }

    public static String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        if (Objects.isNull(messageSource)) {
            log.debug("MessageSource not config,please config i18n messageSource");
            return StringPool.EMPTY;
        }
        return messageSource.getMessage(resolvable, locale);
    }

    public static String getMessage(String code, Object... args) {
        if (Objects.isNull(messageSource)) {
            log.debug("MessageSource not config,please config i18n messageSource");
            return StringPool.EMPTY;
        }
        if (StringUtils.isEmpty(code)) {
            return StringPool.EMPTY;
        }
        return messageSource.getMessage(code, args);
    }

    public static String getMessage(String code, String defaultMessage) {
        if (Objects.isNull(messageSource)) {
            log.debug("MessageSource not config,please config i18n messageSource");
            return defaultMessage;
        }

        if (StringUtils.isEmpty(code)) {
            return defaultMessage;
        }
        return messageSource.getMessage(code, defaultMessage);
    }

    public static String getMessage(Enum enumConstant, String defaultMessage) {
        if (Objects.isNull(enumConstant)) {
            log.debug("enumConstant is null");
            return defaultMessage;
        }
        return getMessage(getMessageKey(enumConstant), defaultMessage);

    }

    public static String getMessage(Enum enumConstant, String defaultMessage, Object... args) {
        if (Objects.isNull(enumConstant)) {
            log.debug("enumConstant is null");
            return I18nMessageFormat.format(defaultMessage, args);
        }
        return getMessage(getMessageKey(enumConstant), defaultMessage, args);
    }

    public static String getMessage(String code, String defaultMessage, Object... args) {
        if (Objects.isNull(messageSource)) {
            log.debug("MessageSource not config,please config i18n messageSource");
            return I18nMessageFormat.format(defaultMessage, args);
        }
        if (StringUtils.isEmpty(code)) {
            return I18nMessageFormat.format(defaultMessage, args);
        }
        return messageSource.getMessage(code, defaultMessage, args);
    }

    public static String getMessageKey(Enum enumConstant) {
        String className = enumConstant.getClass().getName();
        return className + "." + enumConstant.name();
    }
}
