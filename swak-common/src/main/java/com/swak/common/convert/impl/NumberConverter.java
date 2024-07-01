package com.swak.common.convert.impl;

import com.swak.common.convert.ConvertUtil;
import com.swak.common.convert.Converter;
import com.swak.common.i18n.I18nMessageFormat;
import com.swak.common.util.BiIntFunction;
import com.swak.common.util.GetterUtil;
import com.swak.common.util.date.TemporalAccessorUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * NumberConverter.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class NumberConverter implements Converter<Number> {

    private final Class<? extends Number> targetType;

    /**
     * 构造
     */
    public NumberConverter() {
        this.targetType = Number.class;
    }

    /**
     * 构造<br>
     *
     * @param clazz 需要转换的数字类型，默认 {@link Number}
     */
    public NumberConverter(Class<? extends Number> clazz) {
        this.targetType = (null == clazz) ? Number.class : clazz;
    }

    @Override
    public Number convert(Object value, Number defaultValue, String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return convert(value,dataFormats,this.targetType, ConvertUtil::toStr);
    }

    @Override
    public String convertToStr(Number value, String defaultValue, String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return GetterUtil.getString(value.toString(), defaultValue);
    }

    protected static Number convert(Object value,String[] dataFormats, Class<? extends Number> targetType, BiFunction<Object,String[], String> toStrFunc) {
        // 枚举转换为数字默认为其顺序
        if (value instanceof Enum) {
            return convert(((Enum<?>) value).ordinal(),dataFormats, targetType, toStrFunc);
        }
        BigDecimal bigDecimal;
        if (value instanceof Number) {
            bigDecimal = new BigDecimal(value.toString());
        } else if (value instanceof Date) {
            bigDecimal = new BigDecimal(((Date) value).getTime());
        } else if (value instanceof Calendar) {
            bigDecimal = new BigDecimal(((Calendar) value).getTime().getTime());
        } else if (value instanceof TemporalAccessor) {
            bigDecimal = new BigDecimal(TemporalAccessorUtil.toEpochMilli((TemporalAccessor) value));
        } else {
            final String valueStr = toStrFunc.apply(value,dataFormats);
            if (StringUtils.isEmpty(valueStr)) {
                throw new NumberFormatException();
            }
            if (!NumberUtils.isCreatable(valueStr)) {
                throw new NumberFormatException("For input string: \"" + valueStr + "\"");
            }
            bigDecimal = new BigDecimal(valueStr);
        }
        if (Byte.class == targetType) {
            return bigDecimal.byteValue();
        }
        if (Short.class == targetType) {
            return bigDecimal.shortValueExact();
        }
        if (Integer.class == targetType) {
            return bigDecimal.intValue();
        }
        if (AtomicInteger.class == targetType) {
            return new AtomicInteger(bigDecimal.intValue());
        }
        if (Long.class == targetType) {
            return bigDecimal.longValue();
        }
        if (AtomicLong.class == targetType) {
            return new AtomicLong(bigDecimal.longValue());
        }
        if (Float.class == targetType) {
            return bigDecimal.floatValue();
        }
        if (Double.class == targetType) {
            return bigDecimal.doubleValue();
        }
        if (BigDecimal.class == targetType) {
            return bigDecimal;
        }
        if (BigInteger.class == targetType) {
            return bigDecimal.toBigInteger();
        }
        if (Number.class == targetType) {
            if (value instanceof Number) {
                return (Number) value;
            }
            if (value instanceof Boolean) {
                return (Boolean) value ? 1 : 0;
            }
            return NumberUtils.createNumber(bigDecimal.toString());
        }
        throw new UnsupportedOperationException(I18nMessageFormat.format("Unsupport Number type: {}", targetType.getName()));
    }
}
