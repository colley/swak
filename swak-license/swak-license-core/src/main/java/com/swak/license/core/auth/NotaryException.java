/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.core.auth;


import com.swak.license.api.i18n.Message;

import java.security.GeneralSecurityException;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

/**
 * Indicates that a notary could not access the private or public key in a
 * keystore due to insufficient or incorrect configuration parameters.
 */
public class NotaryException extends GeneralSecurityException {

    private static final long serialVersionUID = 0L;

    private final Message msg;

    /**
     * Constructs a notary exception with the given message.
     *
     * @param msg the message.
     */
    public NotaryException(final Message msg) {
        this.msg = requireNonNull(msg);
    }

    @Override
    public String getMessage() { return msg.toString(Locale.ROOT); }

    @Override
    public String getLocalizedMessage() { return msg.toString(); }
}
