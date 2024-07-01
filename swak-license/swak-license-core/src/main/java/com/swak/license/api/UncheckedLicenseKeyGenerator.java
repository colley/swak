/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api;

import com.swak.license.api.io.Sink;

/**
 * A license key generator which generally throws an
 * {@link UncheckedLicenseManagementException} rather than a (checked)
 * {@link LicenseManagementException}.
 *
 * @see UncheckedVendorLicenseManager#generateKeyFrom(License)
 */
public interface UncheckedLicenseKeyGenerator extends LicenseKeyGenerator {

    @Override
    License license() throws UncheckedLicenseManagementException;

    @Override
    UncheckedLicenseKeyGenerator saveTo(Sink sink) throws UncheckedLicenseManagementException;
}
