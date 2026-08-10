/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api;


import com.swak.license.api.i18n.Message;
import lombok.Getter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Indicates that a
 * {@linkplain LicenseValidation#validate(License) license validation} failed.
 */
public class LicenseValidationException extends LicenseManagementException {

    private static final long serialVersionUID = 0L;

    private final Message msg;

    @Getter
    private License license;


    /**
     * Constructs a license validation exception with the given
     * internationalized message.
     *
     * @param msg the internationalized message.
     */
    public LicenseValidationException(final Message msg) {
        this.msg = requireNonNull(msg);
    }
    public LicenseValidationException(final Message msg,License license) {
        this.msg = requireNonNull(msg);
        this.license = license;
    }

    @Override
    public String getMessage() {
         Locale locale = Optional.ofNullable(LocaleContextHolder.getLocale()).orElse(Locale.ROOT);
        return msg.toString(locale);
    }

    @Override
    public String getLocalizedMessage() {
        Locale locale = Optional.ofNullable(LocaleContextHolder.getLocale()).orElse(Locale.getDefault());
        return msg.toString(locale);
    }

    /**
     * Returns {@code true} if this exception is considered confidential and
     * should not be shared with users.
     * <p>
     * The implementation in the class {@code LicenseValidationException}
     * returns {@code false}.
     */
    @Override public boolean isConfidential() { return false; }
}
