/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.core.spi;


import java.util.Locale;

/**
 * A basic message implementation.
 */
public  class DefaultMessage extends BasicMessage {
    private final String message;

    public DefaultMessage(String message){
        this.message = message;
    }

    @Override
    public String toString(Locale locale) {
        return message;
    }
}
