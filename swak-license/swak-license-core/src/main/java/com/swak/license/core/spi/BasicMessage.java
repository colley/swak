
package com.swak.license.core.spi;

import com.swak.license.api.i18n.Message;

import java.util.Locale;

/**
 * A basic message implementation.
 */
public abstract class BasicMessage implements Message {
    @Override
    public final String toString() { return toString(Locale.getDefault()); }
}
