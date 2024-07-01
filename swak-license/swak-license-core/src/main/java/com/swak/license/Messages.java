/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license;

import com.swak.license.api.i18n.Message;
import com.swak.license.core.spi.FormattedMessage;

public final class Messages {


   public static final String CONSUMER_AMOUNT_IS_NOT_POSITIVE = "consumerAmountIsNotPositive";


    public static final String CONSUMER_TYPE_IS_NULL = "consumerTypeIsNull";


    public static final String HOLDER_IS_NULL = "holderIsNull";


    public static final String INVALID_SUBJECT = "invalidSubject";


    public static final String ISSUED_IS_NULL = "issuedIsNull";


    public  static final String ISSUER_IS_NULL = "issuerIsNull";


    public static final String LICENSE_HAS_EXPIRED = "licenseHasExpired";


    public static final String LICENSE_IS_NOT_YET_VALID = "licenseIsNotYetValid";

    /** The message key for the canonical name of an unknown user. */
    public static final String UNKNOWN = "unknown";

    private static final Class<?> BASE_CLASS = Messages.class;

    public static Message message(String key, Object... args) {
        return new FormattedMessage(BASE_CLASS, key, args);
    }

    private Messages() { }
}
