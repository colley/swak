/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.crypto;

/** Creates a password based encryption transformation from some given parameters. */
public interface EncryptionFactory {

    /** Returns a password based encryption from the given parameters. */
    Encryption encryption(EncryptionParameters encryptionParameters);
}
