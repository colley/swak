/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.auth;


import com.swak.license.api.io.Decoder;

/** Provides authentication services. */
public interface Authentication {

    /**
     * Encodes and signs the given {@code artifact} and returns a decoder for it.
     * As a side effect, the state of the underlying repository model is updated with the encoded artifact and its
     * signature so that a subsequent {@linkplain #verify verification} can succeed.
     *
     * @param controller the controller for the repository for encoding the artifact to.
     * @param artifact the artifact to sign.
     * @return A decoder for the signed artifact in the repository.
     */
    Decoder sign(RepositoryController controller, Object artifact) throws Exception;

    /**
     * Verifies the signature of the encoded artifact in the underlying repository model and returns a decoder for it.
     * The state of the underlying repository model is not modified by this method.
     *
     * @param controller the controller for the repository for decoding the artifact from.
     * @return A decoder for the verified artifact in the repository.
     * @throws RepositoryIntegrityException if the integrity of the repository with its encoded artifact has been
     *         compromised.
     */
    Decoder verify(RepositoryController controller) throws Exception;
}
