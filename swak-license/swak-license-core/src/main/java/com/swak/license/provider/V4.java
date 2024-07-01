/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.provider;

import com.swak.license.api.LicenseManagementContextBuilder;
import com.swak.license.core.Core;
import com.swak.license.core.auth.OpensslNotary;

import static com.swak.license.api.io.bios.BIOS.base64;
import static com.swak.license.api.io.bios.BIOS.deflate;

/**
 * This facade provides a static factory method for license management context builders for use with Version 4 (V4)
 * format license keys.
 * This class should not be used by applications because the created license management context builders are only
 * partially configured.
 */
public final class V4 {

    private static final String ENCRYPTION_ALGORITHM = "PBEWithHmacSHA256AndAES_128";


    private static final String KEYSTORE_TYPE = "PKCS12";

    /**
     * Returns a new license management context builder for managing V4 format license keys.
     */
    public static LicenseManagementContextBuilder builder() {
        return Core
                .builder()
                .codecFactory(new V4CodecFactory())
                .authenticationFactory(OpensslNotary::new)
                .compression(base64())
                .encryptionAlgorithm(ENCRYPTION_ALGORITHM)
                .encryptionFactory(V4Encryption::new)
                .keystoreType(KEYSTORE_TYPE)
                .licenseFactory(new V4LicenseFactory())
                .repositoryFactory(new V4RepositoryFactory());
    }

    private V4() {
    }
}
