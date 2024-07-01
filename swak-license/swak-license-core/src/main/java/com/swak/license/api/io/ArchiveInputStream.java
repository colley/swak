/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.io;

import java.io.Closeable;
import java.util.Optional;

/**
 * An abstraction for reading archive entries from an archive file.
 *
 * @see ArchiveOutputStream
 * @author Christian Schlichtherle
 */
public interface ArchiveInputStream extends Iterable<ArchiveEntrySource>, Closeable {

    /**
     * Returns a source for reading the archive entry with the given name, if it exists (optional operation).
     *
     * @param name the archive entry name, where elements are separated by {@code /} and directory names end with a
     *             {@code /}.
     * @throws UnsupportedOperationException if this operation is not supported.
     *                                       This would typically happen with archive files which lack a directory for
     *                                       random access, e.g. the TAR file format.
     * @throws IllegalArgumentException if the normalized form of the given archive entry name is absolute or empty or
     *                                  has {@code ..} as its first path segment.
     */
    Optional<ArchiveEntrySource> source(String name);
}
