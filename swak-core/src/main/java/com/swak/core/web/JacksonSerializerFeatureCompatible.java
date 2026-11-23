package com.swak.core.web;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

import java.util.List;

import static com.swak.core.web.JacksonSerializerFeatureCompatible.SerializerFeature.*;

/**
 * Jackson 3 版本的「null 值序列化特性」兼容实现。
 * <p>
 * Jackson 3 中 {@code BeanSerializerModifier} 已更名为 {@link ValueSerializerModifier}，
 * {@code JsonSerializer} 更名为 {@link ValueSerializer}，{@code SerializerProvider} 更名为
 * {@link SerializationContext}。
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class JacksonSerializerFeatureCompatible extends ValueSerializerModifier {

    private final ValueSerializer<Object> nullBooleanJsonSerializer;
    final private ValueSerializer<Object> nullNumberJsonSerializer;
    final private ValueSerializer<Object> nullListJsonSerializer;
    final private ValueSerializer<Object> nullStringJsonSerializer;

    public JacksonSerializerFeatureCompatible(SerializerFeature... features) {
        int config = 0;
        for (SerializerFeature feature : features) {
            config |= feature.mask;
        }
        nullBooleanJsonSerializer = (config & WriteNullBooleanAsFalse.mask) != 0 ? new NullBooleanSerializer() : null;
        nullNumberJsonSerializer = (config & WriteNullNumberAsZero.mask) != 0 ? new NullNumberSerializer() : null;
        nullListJsonSerializer = (config & WriteNullListAsEmpty.mask) != 0 ? new NullListJsonSerializer() : null;
        nullStringJsonSerializer = (config & WriteNullStringAsEmpty.mask) != 0 ? new NullStringSerializer() : null;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription.Supplier beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            final JavaType javaType = writer.getType();
            final Class<?> rawClass = javaType.getRawClass();
            if (javaType.isArrayType() || javaType.isCollectionLikeType()) {
                writer.assignNullSerializer(nullListJsonSerializer);
            } else if (Number.class.isAssignableFrom(rawClass) && rawClass.getName().startsWith("java.lang")) {
                writer.assignNullSerializer(nullNumberJsonSerializer);
            } else if (Boolean.class.equals(rawClass)) {
                writer.assignNullSerializer(nullBooleanJsonSerializer);
            } else if (String.class.equals(rawClass)) {
                writer.assignNullSerializer(nullStringJsonSerializer);
            }
        }
        return beanProperties;
    }

    private static class NullListJsonSerializer extends ValueSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeStartArray();
            jgen.writeEndArray();
        }
    }

    private static class NullNumberSerializer extends ValueSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeNumber(0);
        }
    }

    private static class NullBooleanSerializer extends ValueSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeBoolean(false);
        }
    }

    private static class NullStringSerializer extends ValueSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeString("");
        }
    }

    public static enum SerializerFeature {
        WriteNullListAsEmpty,
        WriteNullStringAsEmpty,
        WriteNullNumberAsZero,
        WriteNullBooleanAsFalse;

        public final int mask;

        SerializerFeature() {
            mask = (1 << ordinal());
        }
    }
}
