package com.swak.common.convert;


import com.swak.common.util.DatePattern;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Objects;

/**
 * 类型转换器
 */
public class ConvertUtil {

    public static String toStr(Object value, String defaultValue, String[] dateFormat) {
        return ConverterRegistry.getInstance().convertToStr(value, defaultValue, dateFormat);
    }

    public static String toStr(Object value, String defaultValue) {
        return toStr(value, defaultValue, null);
    }

    public static String toDateStr(Object value, String... format) {
        if(Objects.isNull(value)){
            return null;
        }
        if (value instanceof Date || value instanceof  TemporalAccessor) {
            return toStr(value, null, format);
        }
        return toStr(toDate(value, format), null, format);
    }

    public static String toStr(Object value, String[] dateFormat) {
        return toStr(value, null, dateFormat);
    }

    public static String toStr(Object value) {
        return toStr(value, null, null);
    }

    public static Character toChar(Object value, Character defaultValue) {
        return convertQuietly(Character.class, value, defaultValue);
    }


    public static Character toChar(Object value) {
        return toChar(value, null);
    }

    public static Byte toByte(Object value, Byte defaultValue) {
        return convertQuietly(Byte.class, value, defaultValue);
    }

    public static Byte toByte(Object value) {
        return toByte(value, null);
    }

    public static Short toShort(Object value, Short defaultValue) {
        return convertQuietly(Short.class, value, defaultValue);
    }

    public static Short toShort(Object value) {
        return toShort(value, null);
    }

    public static Number toNumber(Object value, Number defaultValue) {
        return convertQuietly(Number.class, value, defaultValue);
    }

    public static Number toNumber(Object value) {
        return toNumber(value, null);
    }

    public static Integer toInt(Object value, Integer defaultValue) {
        return convertQuietly(Integer.class, value, defaultValue);
    }


    public static Integer toInt(Object value) {
        return toInt(value, null);
    }

    public static Long toLong(Object value, Long defaultValue) {
        return convertQuietly(Long.class, value, defaultValue);
    }


    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    public static Double toDouble(Object value, Double defaultValue) {
        return convertQuietly(Double.class, value, defaultValue);
    }

    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    public static Float toFloat(Object value, Float defaultValue) {
        return convertQuietly(Float.class, value, defaultValue);
    }

    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }


    public static Boolean toBool(Object value, Boolean defaultValue) {
        return convertQuietly(Boolean.class, value, defaultValue);
    }

    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }


    public static BigInteger toBigInteger(Object value, BigInteger defaultValue) {
        return convertQuietly(BigInteger.class, value, defaultValue);
    }

    public static BigInteger toBigInteger(Object value) {
        return toBigInteger(value, null);
    }

    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue) {
        return convertQuietly(BigDecimal.class, value, defaultValue);
    }

    public static BigDecimal toBigDecimal(Object value) {
        return toBigDecimal(value, null);
    }


    public static LocalDateTime toLocalDateTime(Object value, LocalDateTime defaultValue, String dateFormat) {
        return convertQuietly(LocalDateTime.class, value, defaultValue, dateFormat);
    }

    public static LocalDateTime toLocalDateTime(Object value) {
        return toLocalDateTime(value, null, null);
    }

    public static LocalDateTime toLocalDateTime(Object value, String dateFormat) {
        return toLocalDateTime(value, null, dateFormat);
    }

    public static Date toDate(Object value, Date defaultValue, String... format) {
        return convertQuietly(Date.class, value, defaultValue, format);
    }

    public static Date toDate(Object value, Date defaultValue) {
        return convertQuietly(Date.class, value, defaultValue, (String) null);
    }

    public static Date toDate(Object value) {
        return toDate(value, (Date) null, (String) null);
    }

    public static Date toDate(Object value, String... format) {
        return toDate(value, null, format);
    }


    public static <T> T convert(Class<T> type, Object value) {
        return convert((Type) type, value, null, (String) null);
    }

    public static <T> T convert(Type type, Object value) {
        return convert(type, value, null, (String) null);
    }

    public static <T> T convert(Class<T> type, Object value, T defaultValue, String... dateFormat) {
        return convert((Type) type, value, defaultValue, dateFormat);
    }

    public static <T> T convert(Type type, Object value, T defaultValue, String... dateFormat) {
        return convertWithCheck(type, value, defaultValue, false, dateFormat);
    }

    public static <T> T convertQuietly(Type type, Object value, T defaultValue, String... dateFormats) {
        return convertWithCheck(type, value, defaultValue, true, dateFormats);
    }

    public static <T> T convertWithCheck(Type type, Object value, T defaultValue, boolean quietly, String... dateFormats) {
        final ConverterRegistry registry = ConverterRegistry.getInstance();
        try {
            return registry.convert(type, value, defaultValue, dateFormats);
        } catch (Exception e) {
            if (quietly) {
                return defaultValue;
            }
            throw e;
        }
    }

    public static void main(String[] args) {
        Object value = "5.01";
        System.out.println(ConvertUtil.toInt(value));
        System.out.println(ConvertUtil.toDouble(value));
        System.out.println(ConvertUtil.toLong(value));
        System.out.println(ConvertUtil.toDateStr(new Date(), DatePattern.NORM_DATETIME_MS_PATTERN));
        System.out.println(ConvertUtil.toDateStr(LocalDateTime.now(), DatePattern.NORM_DATETIME_MS_PATTERN));
        System.out.println(ConvertUtil.toDate("2024-06-26 14:56:32.336"));
        System.out.println(ConvertUtil.toDate("2024-06-26 14:56:32"));
    }
}
