package com.swak.common.convert.impl;

import com.swak.common.convert.ConvertUtil;
import com.swak.common.convert.Converter;
import com.swak.common.i18n.I18nMessageFormat;
import com.swak.common.util.GetterUtil;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * PrimitiveConverter.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class PrimitiveConverter implements Converter<Object> {

    private final Class<?> targetType;
    public PrimitiveConverter(Class<?> clazz) {
        if (null == clazz) {
            throw new NullPointerException("PrimitiveConverter not allow null target type!");
        } else if (false == clazz.isPrimitive()) {
            throw new IllegalArgumentException("[" + clazz + "] is not a primitive class!");
        }
        this.targetType = clazz;
    }

    @Override
    public Object convert(Object value, Object defaultValue, String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return PrimitiveConverter.convert(value,dataFormats, this.targetType, ConvertUtil::toStr);
    }

    @Override
    public String convertToStr(Object value, String defaultValue, String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return value.toString();
    }


    @SuppressWarnings("unchecked")
    public Class<Object> getTargetType() {
        return (Class<Object>) this.targetType;
    }

    /**
     * 将指定值转换为原始类型的值
     *
     * @param value          值
     * @param primitiveClass 原始类型
     * @param toStringFunc   当无法直接转换时，转为字符串后再转换的函数
     * @return 转换结果
     * @since 5.5.0
     */
    protected static Object convert(Object value,String[] dataFormats, Class<?> primitiveClass, BiFunction<Object,String[], String> toStringFunc) {
        if (byte.class == primitiveClass) {
            return GetterUtil.defaultIfNull(NumberConverter.convert(value,dataFormats, Byte.class, toStringFunc), 0);
        } else if (short.class == primitiveClass) {
            return GetterUtil.defaultIfNull(NumberConverter.convert(value,dataFormats, Short.class, toStringFunc), 0);
        } else if (int.class == primitiveClass) {
            return GetterUtil.defaultIfNull(NumberConverter.convert(value,dataFormats, Integer.class, toStringFunc), 0);
        } else if (long.class == primitiveClass) {
            return GetterUtil.defaultIfNull(NumberConverter.convert(value,dataFormats, Long.class, toStringFunc), 0);
        } else if (float.class == primitiveClass) {
            return GetterUtil.defaultIfNull(NumberConverter.convert(value,dataFormats, Float.class, toStringFunc), 0);
        } else if (double.class == primitiveClass) {
            return GetterUtil.defaultIfNull(NumberConverter.convert(value,dataFormats, Double.class, toStringFunc), 0);
        } else if (char.class == primitiveClass) {
            return ConvertUtil.convert(Character.class, value);
        } else if (boolean.class == primitiveClass) {
            return ConvertUtil.convert(Boolean.class, value);
        }
        throw new UnsupportedOperationException(I18nMessageFormat.format("Unsupported target type: {}", primitiveClass));
    }
}
