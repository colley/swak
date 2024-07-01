/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.core.passwd;


import com.swak.license.api.passwd.Password;
import com.swak.license.api.passwd.PasswordProtection;
import com.swak.license.api.passwd.PasswordUsage;
import com.swak.common.util.ObfuscatedString;

import java.util.Arrays;
import java.util.Objects;

/**
 * A password protection which uses an obfuscated string.
 */
public final class ObfuscatedPasswordProtection implements PasswordProtection {

    private final ObfuscatedString os;

    public ObfuscatedPasswordProtection(final ObfuscatedString os) { this.os = Objects.requireNonNull(os); }

    @Override
    public Password password(PasswordUsage usage) { return new ObfuscatedPassword(); }

    private final class ObfuscatedPassword implements Password {

        final char[] characters = os.toCharArray();

        @Override
        public char[] characters() { return characters; }

        @Override
        public void close() { Arrays.fill(characters, (char) 0); }
    }
}
