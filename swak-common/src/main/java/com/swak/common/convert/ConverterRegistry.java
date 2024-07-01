package com.swak.common.convert;


import com.google.common.collect.Maps;
import com.swak.common.convert.impl.*;
import com.swak.common.exception.SwakException;
import com.swak.common.i18n.I18nMessageFormat;
import com.swak.common.util.ClassUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;


public class ConverterRegistry {

    /**
     * 默认类型转换器
     */
    private Map<Class<?>, Converter<?>> defaultConverterMap = Maps.newConcurrentMap();

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static final ConverterRegistry INSTANCE = new ConverterRegistry();
    }

    public static ConverterRegistry getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 构造
     */
    public ConverterRegistry() {
        defaultConverter();
    }


    public <T> Converter<T> getConverter(Type type) {
        return (Converter<T>) defaultConverterMap.get(ClassUtils.getClass(type));
    }

    public String convertToStr(Object value,String defaultValue,String...dateFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        Converter<Object> converter = getConverter(value.getClass());
        if (Objects.nonNull(converter)) {
            return converter.convertToStr(value,defaultValue,dateFormats);
        }
        return defaultValue;
    }

    public <T> T convert(Type type, Object value, T defaultValue,String...dateFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        Converter<T> converter = getConverter(type);
        if (Objects.isNull(converter)) {
            type = defaultValue.getClass();
            converter = getConverter(type);
        }
        if (null != converter) {
            return converter.convert(value, defaultValue,dateFormats);
        }
        // 无法转换
        throw new SwakException(I18nMessageFormat.format("Can not Converter from [{}] to [{}]", value.getClass().getName(), type.getTypeName()));
    }


    /**
     * 注册默认转换器
     *
     * @return 转换器
     */
    private ConverterRegistry defaultConverter() {
        // 原始类型转换器
        defaultConverterMap.put(int.class, new PrimitiveConverter(int.class));
        defaultConverterMap.put(long.class, new PrimitiveConverter(long.class));
        defaultConverterMap.put(byte.class, new PrimitiveConverter(byte.class));
        defaultConverterMap.put(short.class, new PrimitiveConverter(short.class));
        defaultConverterMap.put(float.class, new PrimitiveConverter(float.class));
        defaultConverterMap.put(double.class, new PrimitiveConverter(double.class));
        defaultConverterMap.put(char.class, new PrimitiveConverter(char.class));
        defaultConverterMap.put(boolean.class, new PrimitiveConverter(boolean.class));

        // 包装类转换器
        defaultConverterMap.put(Number.class, new NumberConverter());
        defaultConverterMap.put(Integer.class, new NumberConverter(Integer.class));
        defaultConverterMap.put(AtomicInteger.class, new NumberConverter(AtomicInteger.class));// since 3.0.8
        defaultConverterMap.put(Long.class, new NumberConverter(Long.class));
        defaultConverterMap.put(LongAdder.class, new NumberConverter(LongAdder.class));
        defaultConverterMap.put(AtomicLong.class, new NumberConverter(AtomicLong.class));// since 3.0.8
        defaultConverterMap.put(Byte.class, new NumberConverter(Byte.class));
        defaultConverterMap.put(Short.class, new NumberConverter(Short.class));
        defaultConverterMap.put(Float.class, new NumberConverter(Float.class));
        defaultConverterMap.put(Double.class, new NumberConverter(Double.class));
        defaultConverterMap.put(DoubleAdder.class, new NumberConverter(DoubleAdder.class));
        defaultConverterMap.put(Character.class, new CharacterConverter());
        defaultConverterMap.put(Boolean.class, new BooleanConverter());
        defaultConverterMap.put(BigDecimal.class, new NumberConverter(BigDecimal.class));
        defaultConverterMap.put(BigInteger.class, new NumberConverter(BigInteger.class));
        defaultConverterMap.put(CharSequence.class, new StringConverter());
        defaultConverterMap.put(String.class, new StringConverter());

        // 日期时间
        defaultConverterMap.put(Calendar.class, new CalendarConverter());
        defaultConverterMap.put(java.util.Date.class, new DateConverter(java.util.Date.class));
        defaultConverterMap.put(java.sql.Date.class, new DateConverter(java.sql.Date.class));
        defaultConverterMap.put(java.sql.Time.class, new DateConverter(java.sql.Time.class));
        defaultConverterMap.put(java.sql.Timestamp.class, new DateConverter(java.sql.Timestamp.class));

        // 日期时间 JDK8+(since 5.0.0)
        defaultConverterMap.put(TemporalAccessor.class, new TemporalAccessorConverter(Instant.class));
        defaultConverterMap.put(Instant.class, new TemporalAccessorConverter(Instant.class));
        defaultConverterMap.put(LocalDateTime.class, new TemporalAccessorConverter(LocalDateTime.class));
        defaultConverterMap.put(LocalDate.class, new TemporalAccessorConverter(LocalDate.class));
        defaultConverterMap.put(LocalTime.class, new TemporalAccessorConverter(LocalTime.class));
        defaultConverterMap.put(ZonedDateTime.class, new TemporalAccessorConverter(ZonedDateTime.class));
        defaultConverterMap.put(OffsetDateTime.class, new TemporalAccessorConverter(OffsetDateTime.class));
        defaultConverterMap.put(OffsetTime.class, new TemporalAccessorConverter(OffsetTime.class));
        defaultConverterMap.put(DayOfWeek.class, new TemporalAccessorConverter(DayOfWeek.class));
        defaultConverterMap.put(Month.class, new TemporalAccessorConverter(Month.class));
        defaultConverterMap.put(MonthDay.class, new TemporalAccessorConverter(MonthDay.class));
        return this;
    }
}
