/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api;

/**
 * A runtime exception which wraps a (checked) {@link LicenseManagementException}.
 * This is generally thrown by the {@code Unchecked*} interfaces.
 */
public class UncheckedLicenseManagementException extends RuntimeException {

    private static final long serialVersionUID = 0L;

    public UncheckedLicenseManagementException(Throwable cause) {
        super(cause);
    }
}
