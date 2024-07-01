/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.io.bios;


import com.swak.license.api.io.ArchiveEntrySink;
import com.swak.license.api.io.ArchiveOutputStream;
import com.swak.license.api.io.spi.ArchiveEntryNames;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;


/**
 * Adapts a {@link JarOutputStream} to an {@link ArchiveOutputStream}.
 *
 * @author Christian Schlichtherle
 */
final class JarOutputStreamAdapter extends ZipOutputStreamAdapter {

    JarOutputStreamAdapter(JarOutputStream jar) {
        super(jar);
    }

    /**
     * Returns {@code true}.
     */
    @Override
    public boolean isJar() {
        return true;
    }

    @Override
    public ArchiveEntrySink sink(String name) {
        return sink(new JarEntry(ArchiveEntryNames.requireInternal(name)));
    }
}
