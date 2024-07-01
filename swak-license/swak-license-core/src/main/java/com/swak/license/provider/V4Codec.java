/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.provider;

import com.swak.license.api.Codec;
import com.swak.license.api.io.Decoder;
import com.swak.license.api.io.Encoder;
import com.swak.license.api.io.Socket;

import java.io.InputStream;
import java.io.OutputStream;

import static com.swak.license.api.io.json.Jackson.json;


/**
 * A codec for use with V4 format license keys.
 */
final class V4Codec implements Codec {


    private static final String CONTENT_TYPE = "application/json";


    private static final String CONTENT_TRANSFER_ENCODING = "8bit";

    private final com.swak.license.api.io.Codec codec;

    V4Codec(V4CodecFactory factory) {
        this.codec = json(factory::objectMapper);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation in the class {@link V4Codec}
     * returns {@code "application/json"}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc4627">RFC 4627</a>
     */
    @Override
    public String contentType() {
        return CONTENT_TYPE;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation in the class {@link V4Codec}
     * returns {@code "8bit"}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc4627">RFC 4627</a>
     */
    @Override
    public String contentTransferEncoding() {
        return CONTENT_TRANSFER_ENCODING;
    }

    @Override
    public Encoder encoder(Socket<OutputStream> output) {
        return codec.encoder(output);
    }

    @Override
    public Decoder decoder(Socket<InputStream> input) {
        return codec.decoder(input);
    }
}
