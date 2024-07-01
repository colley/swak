/*
 * Copyright (C) 2013-2018 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.io;

/**
 * An abstraction for safe access to an {@link ArchiveInputStream archive input stream}.
 *
 * @author Christian Schlichtherle
 */
@FunctionalInterface
public interface ArchiveSource extends GenSource<ArchiveInputStream> {
}
