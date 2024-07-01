
package com.swak.license.api.io.bios;


import com.swak.license.api.io.Filter;
import com.swak.license.api.io.Socket;
import com.swak.license.api.io.Store;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Christian Schlichtherle
 */
final class GZIPFilter implements Filter {

    @Override
    public Socket<OutputStream> output(Socket<OutputStream> output) {
        return output.map(out -> new GZIPOutputStream(out, Store.BUFSIZE));
    }

    @Override
    public Socket<InputStream> input(Socket<InputStream> input) {
        return input.map(in -> new GZIPInputStream(in, Store.BUFSIZE));
    }
}
