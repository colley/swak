/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.io.bios;

import com.swak.license.api.io.ArchiveEntrySource;
import com.swak.license.api.io.ArchiveInputStream;
import com.swak.license.api.io.Socket;
import com.swak.license.api.io.spi.ArchiveEntryNames;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link ZipFile} to an {@link ArchiveInputStream}.
 *
 * @author Christian Schlichtherle
 */
final class ZipFileAdapter implements ArchiveInputStream {

    private final ZipFile zip;

    ZipFileAdapter(final ZipFile input) {
        this.zip = requireNonNull(input);
    }

    @Override
    public Iterator<ArchiveEntrySource> iterator() {
        return new Iterator<ArchiveEntrySource>() {

            final Enumeration<? extends ZipEntry> en = zip.entries();
            ZipEntry next;

            @Override
            public boolean hasNext() {
                if (null != next) {
                    return true;
                } else {
                    while (en.hasMoreElements()) {
                        final ZipEntry entry = en.nextElement();
                        if (ArchiveEntryNames.isInternal(entry.getName())) {
                            next = entry;
                            return true;
                        }
                    }
                    return false;
                }
            }

            @Override
            public ArchiveEntrySource next() {
                if (hasNext()) {
                    final ZipEntry entry = next;
                    next = null;
                    return source(entry);
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public Optional<ArchiveEntrySource> source(String name) {
        return Optional.ofNullable(zip.getEntry(ArchiveEntryNames.requireInternal(name))).map(this::source);
    }

    private ArchiveEntrySource source(ZipEntry entry) {
        return new ArchiveEntrySource() {

            @Override
            public Socket<InputStream> input() {
                return () -> zip.getInputStream(entry);
            }

            @Override
            public String name() {
                return entry.getName();
            }

            @Override
            public boolean directory() {
                return entry.isDirectory();
            }

            @Override
            public long size() {
                return entry.getSize();
            }
        };
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}
