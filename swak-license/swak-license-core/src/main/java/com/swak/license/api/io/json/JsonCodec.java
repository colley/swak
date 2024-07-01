
package com.swak.license.api.io.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swak.license.api.io.Codec;
import com.swak.license.api.io.Decoder;
import com.swak.license.api.io.Encoder;
import com.swak.license.api.io.Socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

final class JsonCodec implements Codec {

    private final ObjectMapper mapper;

    JsonCodec(final ObjectMapper m) { this.mapper = m; }

    @Override
    public Encoder encoder(Socket<OutputStream> output) {
        return obj -> output.accept(out -> mapper.writeValue(out, obj));
    }

    @Override
    public Decoder decoder(final Socket<InputStream> input) {
        return new Decoder() {
            @Override
            public <T> T decode(Type expected) throws Exception {
                return input.apply(in -> mapper.readValue(in, mapper.constructType(expected)));
            }
        };
    }
}
