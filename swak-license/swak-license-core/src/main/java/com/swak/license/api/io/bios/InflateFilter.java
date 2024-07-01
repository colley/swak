
package com.swak.license.api.io.bios;


import com.swak.license.api.io.Filter;
import com.swak.license.api.io.Socket;
import com.swak.license.api.io.Store;
import com.swak.license.api.io.function.XSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * @author Christian Schlichtherle
 */
final class InflateFilter implements Filter {

    private final XSupplier<Inflater> inflaterSupplier;
    private final XSupplier<Deflater> deflaterSupplier;

    InflateFilter(final XSupplier<Inflater> inflaterSupplier, final XSupplier<Deflater> deflaterSupplier) {
        this.inflaterSupplier = inflaterSupplier;
        this.deflaterSupplier = deflaterSupplier;
    }

    @Override
    public Socket<OutputStream> output(final Socket<OutputStream> output) {
        return output.map(out -> new InflaterOutputStream(out, inflaterSupplier.get(), Store.BUFSIZE) {

            boolean closed;

            @Override
            public void close() throws IOException {
                finish();
                if (!closed) {
                    closed = true;
                    SideEffect.runAll(inf::end, super::close);
                }
            }
        });
    }

    @Override
    public Socket<InputStream> input(final Socket<InputStream> input) {
        return input.map(in -> new DeflaterInputStream(in, deflaterSupplier.get(), Store.BUFSIZE) {

            boolean closed;

            @Override
            public void close() throws IOException {
                if (!closed) {
                    closed = true;
                    SideEffect.runAll(def::end, super::close);
                }
            }
        });
    }
}
