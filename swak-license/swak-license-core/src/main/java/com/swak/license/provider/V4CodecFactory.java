/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.swak.license.api.Codec;
import com.swak.license.api.CodecFactory;
import com.swak.license.api.License;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;

/**
 * A codec factory for use with V4 format license keys.
 */
@SuppressWarnings("WeakerAccess")
public class V4CodecFactory implements CodecFactory {

    @Override
    public final Codec codec() {
        return new V4Codec(this);
    }

    /**
     * Returns a new object mapper.
     */
    public ObjectMapper objectMapper() {
        return configure(new ObjectMapper());
    }

    /**
     * Configures and returns the given object mapper.
     */
    protected final ObjectMapper configure(final ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        final SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(License.class, V4License.class);
        module.addSerializer(new StdSerializer<X500Principal>(X500Principal.class) {

            @Override
            public void serialize(X500Principal value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString(value.getName());
            }
        });
        module.addDeserializer(X500Principal.class, new StdDeserializer<X500Principal>(X500Principal.class) {

            @Override
            public X500Principal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new X500Principal(p.readValueAs(String.class));
            }
        });
        mapper.registerModule(module);
        return mapper;
    }
}
