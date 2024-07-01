package com.swak.license.api;

import com.swak.license.api.auth.Authentication;
import com.swak.license.api.auth.AuthenticationChildBuilder;
import com.swak.license.api.builder.GenChildBuilder;
import com.swak.license.api.crypto.EncryptionChildBuilder;

/**
 * A mix-in for a builder for license managers.
 *
 * @param <This> the specialized type for fluent programming.
 */

import com.swak.license.api.io.Filter;

public interface LicenseManagerBuilderMixin<This extends LicenseManagerBuilderMixin<This>> {

    /**
     * Returns an injection for a keystore based authentication.
     * Call its {@link GenChildBuilder#up} method to build and inject the
     * configured authentication into this builder and return it.
     *
     * @see #authentication(Authentication)
     */
    AuthenticationChildBuilder<? extends This> authentication();

    /**
     * Sets the authentication.
     *
     * @return {@code this}.
     */
    This authentication(Authentication authentication);

    /**
     * Returns an injection for a password based encryption.
     * Call its {@link GenChildBuilder#up} method to build and inject the
     * configured encryption into this builder and return it.
     *
     * @see #encryption(Filter)
     */
    EncryptionChildBuilder<? extends This> encryption();

    /**
     * Sets the encryption.
     *
     * @return {@code this}.
     */
    This encryption(Filter encryption);
}
