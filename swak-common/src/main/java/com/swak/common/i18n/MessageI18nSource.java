package com.swak.common.i18n;


import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.util.Locale;

public interface MessageI18nSource {

    String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException;

    String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

    String getMessage(String code, Object... args);

    String getMessage(String code, String defaultMessage);

    String getMessage(String code, String defaultMessage, Object... args);
}
