/*
 * Copyright © 2017 - 2020 Schlichtherle IT Services
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
import com.swak.license.api.io.Store;
import com.swak.license.api.io.function.XSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author Christian Schlichtherle
 */
final class DeflateFilter implements Filter {

    private final XSupplier<Deflater> deflaterSupplier;
    private final XSupplier<Inflater> inflaterSupplier;

    DeflateFilter(final XSupplier<Deflater> deflaterSupplier, final XSupplier<Inflater> inflaterSupplier) {
        this.deflaterSupplier = deflaterSupplier;
        this.inflaterSupplier = inflaterSupplier;
    }

    @Override
    public Socket<OutputStream> output(final Socket<OutputStream> output) {
        return output.map(out -> new DeflaterOutputStream(out, deflaterSupplier.get(), Store.BUFSIZE) {

            boolean closed;

            @Override
            public void close() throws IOException {
                finish();
                if (!closed) {
                    closed = true;
                    SideEffect.runAll(def::end, super::close);
                }
            }
        });
    }

    @Override
    public Socket<InputStream> input(final Socket<InputStream> input) {
        return input.map(in -> new InflaterInputStream(in, inflaterSupplier.get(), Store.BUFSIZE) {

            boolean closed;

            @Override
            public void close() throws IOException {
                if (!closed) {
                    closed = true;
                    SideEffect.runAll(inf::end, super::close);
                }
            }
        });
    }
}
