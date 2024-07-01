/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api;

import com.swak.license.api.auth.Authentication;
import com.swak.license.api.auth.RepositoryFactory;
import com.swak.license.api.io.Filter;

/**
 * A configuration for license management.
 * A configuration is built from a {@linkplain #context() license management context}.
 */
public interface LicenseManagerParameters {

    /**
     * Returns the authentication.
     */
    Authentication authentication();

    /**
     * Returns the codec
     */
    default Codec codec() {
        return context().codec();
    }

    /**
     * Returns the license management context from which these parameters have been built.
     */
    LicenseManagementContext context();

    /**
     * Returns the password based encryption transformation.
     */
    Filter encryption();

    /**
     * Returns the license factory.
     */
    default LicenseFactory licenseFactory() {
        return context().licenseFactory();
    }

    /**
     * Return the repository factory.
     */
    default RepositoryFactory<?> repositoryFactory() {
        return context().repositoryFactory();
    }

    /**
     * Returns the license management subject.
     */
    default String subject() {
        return context().subject();
    }
}
