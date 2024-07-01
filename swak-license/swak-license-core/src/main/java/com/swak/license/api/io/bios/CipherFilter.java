/*
 * Copyright © 2017 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.swak.license.api.io.bios;

import com.swak.license.api.io.Filter;
import com.swak.license.api.io.Socket;
import com.swak.license.api.io.function.XSupplier;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Christian Schlichtherle
 */
final class CipherFilter implements Filter {

    private final XSupplier<Cipher> inputCipherSupplier, outputCipherSupplier;

    CipherFilter(final XSupplier<Cipher> inputCipherSupplier, final XSupplier<Cipher> outputCipherSupplier) {
        this.inputCipherSupplier = inputCipherSupplier;
        this.outputCipherSupplier = outputCipherSupplier;
    }

    @Override
    public Socket<OutputStream> output(Socket<OutputStream> output) {
        return output.map(out -> new CipherOutputStream(out, outputCipherSupplier.get()));
    }

    @Override
    public Socket<InputStream> input(Socket<InputStream> input) {
        return input.map(in -> new CipherInputStream(in, inputCipherSupplier.get()));
    }
}
