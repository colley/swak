/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.provider;

import com.swak.license.api.crypto.Encryption;
import com.swak.license.api.crypto.EncryptionParameters;
import com.swak.license.api.io.Socket;
import com.swak.license.api.passwd.PasswordUsage;
import com.swak.license.core.crypto.EncryptionMixin;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AlgorithmParameters;

import static javax.crypto.Cipher.*;

/**
 * An encryption for use with V4 format license keys.
 */
final class V4Encryption extends EncryptionMixin implements Encryption {

    V4Encryption(EncryptionParameters parameters) {
        super(parameters);
    }

    @Override
    public Socket<OutputStream> output(final Socket<OutputStream> output) {
        return output.map(out -> {
            final Cipher cipher = cipher(PasswordUsage.ENCRYPTION, null);
            final AlgorithmParameters param = cipher.getParameters();
            final byte[] encoded = param.getEncoded();
            assert encoded.length <= Short.MAX_VALUE;
            new DataOutputStream(out).writeShort(encoded.length);
            out.write(encoded);
            return new CipherOutputStream(out, cipher);
        });
    }

    @Override
    public Socket<InputStream> input(final Socket<InputStream> input) {
        return input.map(in -> {
            final DataInputStream din = new DataInputStream(in);
            final byte[] encoded = new byte[din.readShort() & 0xffff];
            din.readFully(encoded);
            return new CipherInputStream(in, cipher(PasswordUsage.DECRYPTION, param(encoded)));
        });
    }

    private Cipher cipher(final PasswordUsage usage, final AlgorithmParameters param) throws Exception {
        final Cipher cipher = getInstance(algorithm());
        cipher.init(PasswordUsage.ENCRYPTION.equals(usage) ? ENCRYPT_MODE : DECRYPT_MODE, secretKey(usage), param);
        return cipher;
    }

    private AlgorithmParameters param(final byte[] encoded) throws Exception {
        final AlgorithmParameters param = AlgorithmParameters.getInstance(algorithm());
        param.init(encoded);
        return param;
    }
}
