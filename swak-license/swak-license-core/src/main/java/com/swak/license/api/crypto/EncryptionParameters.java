/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.crypto;

import com.swak.license.api.passwd.PasswordProtection;

/**
 * Defines parameters for a password based encryption.
 * <p>
 * Unless stated otherwise, all no-argument methods need to return consistent
 * objects so that caching them is not required.
 * A returned object is considered to be consistent if it compares
 * {@linkplain Object#equals(Object) equal} or at least behaves identical to
 * any previously returned object.
 */
public interface EncryptionParameters {

    /** Returns the name of the password based encryption algorithm. */
    String algorithm();

    /** Returns a password protection for generating the secret key for encryption/decryption. */
    PasswordProtection protection();
}
