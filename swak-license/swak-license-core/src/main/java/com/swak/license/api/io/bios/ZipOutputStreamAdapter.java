/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.io.bios;

import com.swak.license.api.io.ArchiveEntrySink;
import com.swak.license.api.io.ArchiveEntrySource;
import com.swak.license.api.io.ArchiveOutputStream;
import com.swak.license.api.io.Socket;
import com.swak.license.api.io.spi.ArchiveEntryNames;
import com.swak.license.api.io.spi.Copy;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.swak.license.api.io.spi.Copy.copy;
import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipOutputStream} to an {@link ArchiveOutputStream}.
 *
 * @author Christian Schlichtherle
 */
class ZipOutputStreamAdapter implements ArchiveOutputStream {

    private final ZipOutputStream zip;

    ZipOutputStreamAdapter(final ZipOutputStream zip) {
        this.zip = requireNonNull(zip);
    }

    @Override
    public ArchiveEntrySink sink(String name) {
        return sink(new ZipEntry(ArchiveEntryNames.requireInternal(name)));
    }

    ArchiveEntrySink sink(ZipEntry entry) {
        return new ArchiveEntrySink() {

            @Override
            public Socket<OutputStream> output() {
                return () -> {
                    if (entry.isDirectory()) {
                        entry.setMethod(ZipOutputStream.STORED);
                        entry.setSize(0);
                        entry.setCompressedSize(0);
                        entry.setCrc(0);
                    }
                    zip.putNextEntry(entry);
                    return new FilterOutputStream(zip) {

                        @Override
                        public void close() throws IOException {
                            zip.closeEntry();
                        }
                    };
                };
            }

            @Override
            public void copyFrom(ArchiveEntrySource source) throws Exception {
                Copy.copy(source, this);
            }
        };
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}
