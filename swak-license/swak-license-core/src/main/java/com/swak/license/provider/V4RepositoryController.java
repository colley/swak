/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.provider;

import com.swak.license.api.Codec;
import com.swak.license.api.auth.RepositoryController;
import com.swak.license.api.auth.RepositoryIntegrityException;
import com.swak.license.api.io.Decoder;
import com.swak.license.api.io.Store;

import java.security.Signature;

import static com.swak.license.api.io.bios.BIOS.memory;
import static com.swak.license.core.spi.Codecs.charset;
import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;
import static java.util.Objects.requireNonNull;

/**
 * A repository controller for use with V4 format license keys.
 */
final class V4RepositoryController implements RepositoryController {

    private final Codec codec;
    private final V4RepositoryModel model;

    V4RepositoryController(final Codec codec, final V4RepositoryModel model) {
        this.codec = requireNonNull(codec);
        this.model = requireNonNull(model);
    }

    @Override
    public final Decoder sign(final Signature engine, final Object artifact) throws Exception {
        final Store store = memory();
        codec.encoder(store).encode(artifact);
        final byte[] artifactData = store.content();
        engine.update(artifactData);
        final byte[] signatureData = engine.sign();

        final String encodedArtifact = body(codec, artifactData);
        final String encodedSignature = getEncoder().encodeToString(signatureData);
        final String signatureAlgorithm = engine.getAlgorithm();

        model.setArtifact(encodedArtifact);
        model.setSignature(encodedSignature);
        model.setAlgorithm(signatureAlgorithm);

        return codec.decoder(store);
    }

    private static String body(Codec codec, byte[] artifact) {
        return charset(codec)
                .map(charset -> new String(artifact, charset))
                .orElseGet(() -> getEncoder().encodeToString(artifact));
    }

    @Override
    public final Decoder verify(final Signature engine) throws Exception {
        if (!engine.getAlgorithm().equalsIgnoreCase(model.algorithm)) {
            throw new IllegalArgumentException();
        }
        final byte[] artifactData = data(codec, model.artifact);
        engine.update(artifactData);
        if (!engine.verify(getDecoder().decode(model.signature))) {
            throw new RepositoryIntegrityException();
        }
        final Store store = memory();
        store.content(artifactData);
        return codec.decoder(store);
    }

    private static byte[] data(Codec codec, String body) {
        return charset(codec).map(body::getBytes).orElseGet(() -> getDecoder().decode(body));
    }
}
