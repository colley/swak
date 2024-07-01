/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.core.spi;


import com.swak.license.api.i18n.Message;

/**
 * A formattable object.
 */
public interface Formattable {

    /**
     * Formats the message keyed by this object with the given arguments.
     *
     * @param args the formatting arguments.
     *             Implementations may add constraints to these.
     * @return the formatted message.
     */
    Message format(Object... args);
}
