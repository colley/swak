/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.core.auth;

import com.swak.license.api.i18n.Message;
import com.swak.license.core.spi.FormattedMessage;

/**
 * Defines message keys in the resource bundle for this package.
 * This class is immutable.
 */
final class Messages {

    private static final Class<?> BASE_CLASS = Messages.class;

    static Message message(String key, Object... args) {
        return new FormattedMessage(BASE_CLASS, key, args);
    }

    private Messages() { }
}
