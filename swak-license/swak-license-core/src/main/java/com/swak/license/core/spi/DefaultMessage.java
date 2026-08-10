
package com.swak.license.core.spi;


import java.util.Locale;

/**
 * A basic message implementation.
 * @author colley
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
