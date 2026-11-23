/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swak.license.api.Codec;
import com.swak.license.api.CodecFactory;
import com.swak.license.api.License;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import javax.security.auth.x500.X500Principal;

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
     * <p>
     * Jackson 3 的 {@code ObjectMapper} 不可变，因此这里通过 {@code rebuild()} 返回新的 mapper。
     */
    protected final ObjectMapper configure(final ObjectMapper mapper) {
        final SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(License.class, V4License.class);
        module.addSerializer(new ValueSerializer<X500Principal>() {

            @Override
            public void serialize(X500Principal value, JsonGenerator gen, SerializationContext provider) {
                gen.writeString(value.getName());
            }
        });
        module.addDeserializer(X500Principal.class, new ValueDeserializer<X500Principal>() {

            @Override
            public X500Principal deserialize(JsonParser p, DeserializationContext ctxt) {
                return new X500Principal(p.readValueAs(String.class));
            }
        });
        return mapper.rebuild()
                .changeDefaultPropertyInclusion(
                        inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_DEFAULT))
                .addModule(module)
                .build();
    }
}
