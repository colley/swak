package com.swak.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swak.common.exception.SwakException;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Jackson 3（tools.jackson）工具类。
 * <p>
 * Jackson 3 的 {@link ObjectMapper} 是不可变的（无 {@code registerModule}/{@code registerSubtypes}
 * 等运行时配置方法），因此子类型注册采用「重新构建 mapper」的方式实现。
 *
 * @author colley.ma
 * @since 3.0.0
 */
public final class JacksonUtils {

    private static final List<NamedType> SUB_TYPES = new ArrayList<>();

    private static volatile ObjectMapper mapper = buildMapper();

    public JacksonUtils() {
    }

    private static ObjectMapper buildMapper() {
        SimpleModule subtypeModule = new SimpleModule("swak-subtype-module");
        synchronized (SUB_TYPES) {
            if (!SUB_TYPES.isEmpty()) {
                subtypeModule.registerSubtypes(SUB_TYPES.toArray(new NamedType[0]));
            }
        }
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(
                        inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL))
                .addModule(subtypeModule)
                .build();
    }

    public static String toJSONString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JacksonException var2) {
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
        } catch (JacksonException var2) {
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
        } catch (JacksonException var3) {
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
        } catch (JacksonException var3) {
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
        } catch (JacksonException var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(String json, Type type) {
        try {
            return mapper.readValue(json, mapper.constructType(type));
        } catch (JacksonException var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (JacksonException var3) {
            throw new SwakException(var3);
        }
    }

    public static <T> T parseObject(InputStream inputStream, Type type) {
        try {
            return mapper.readValue(inputStream, mapper.constructType(type));
        } catch (JacksonException var3) {
            throw new SwakException(var3);
        }
    }

    public static JsonNode parseObject(String json) {
        try {
            return mapper.readTree(json);
        } catch (JacksonException var2) {
            throw new SwakException(var2);
        }
    }

    /**
     * 注册多态子类型。Jackson 3 的 mapper 不可变，此处通过重建 mapper 生效。
     */
    public static void registerSubtype(Class<?> clz, String type) {
        synchronized (SUB_TYPES) {
            SUB_TYPES.add(new NamedType(clz, type));
        }
        mapper = buildMapper();
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
}
