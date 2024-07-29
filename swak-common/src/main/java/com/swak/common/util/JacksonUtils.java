package com.swak.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swak.common.exception.SwakException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public final class JacksonUtils {
    static ObjectMapper mapper = new ObjectMapper();

    public JacksonUtils() {
    }

    public static String toJSONString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException var2) {
            throw new SwakException(var2);
        }
    }

    public static Map<String, Object> convertValue(Object obj) {
        return convertValue(obj, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <T> T convertValue(Object obj, TypeReference<T> toValueTypeRef) {
        return mapper.convertValue(obj, toValueTypeRef);
    }

    public static byte[] toJsonBytes(Object obj) {
        try {
            return ByteUtils.toBytes(mapper.writeValueAsString(obj));
        } catch (JsonProcessingException var2) {
            throw new SwakException(var2);
        }
    }

    public static <T> List<T> parseArray(byte[] json, Class<T> cls) {
        return parseArray(StringUtils.toEncodedString(json, Charset.forName("UTF-8")), cls);
    }

    public static <T> List<T> parseArray(String json, Class<T> cls) {
        try {
            return mapper.readValue(json, new TypeReference<List<T>>() {
            });
        } catch (IOException var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(byte[] json, Class<T> cls) {
        try {
            return parseObject(StringUtils.toEncodedString(json, Charset.forName("UTF-8")), cls);
        } catch (Exception var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(byte[] json, Type cls) {
        try {
            return parseObject(StringUtils.toEncodedString(json, Charset.forName("UTF-8")), cls);
        } catch (Exception var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(InputStream inputStream, Class<T> cls) {
        try {
            return mapper.readValue(inputStream, cls);
        } catch (IOException var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(byte[] json, TypeReference<T> typeReference) {
        try {
            return parseObject(StringUtils.toEncodedString(json, Charset.forName("UTF-8")), typeReference);
        } catch (Exception var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(String json, Class<T> cls) {
        try {
            return mapper.readValue(json, cls);
        } catch (IOException var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(String json, Type type) {
        try {
            return mapper.readValue(json, mapper.constructType(type));
        } catch (IOException var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(InputStream inputStream, Type type) {
        try {
            return mapper.readValue(inputStream, mapper.constructType(type));
        } catch (IOException var3) {
            throw new SwakException(var3);
        }
    }

    public static JsonNode parseObject(String json) {
        try {
            return mapper.readTree(json);
        } catch (IOException var2) {
            throw new SwakException(var2);
        }
    }

    public static void registerSubtype(Class<?> clz, String type) {
        mapper.registerSubtypes(new NamedType[]{new NamedType(clz, type)});
    }

    public static ObjectNode createEmptyJsonNode() {
        return new ObjectNode(mapper.getNodeFactory());
    }

    public static ArrayNode createEmptyArrayNode() {
        return new ArrayNode(mapper.getNodeFactory());
    }

    public static JsonNode transferToJsonNode(Object obj) {
        return mapper.valueToTree(obj);
    }

    public static JavaType constructJavaType(Type type) {
        return mapper.constructType(type);
    }

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
