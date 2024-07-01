package com.swak.common.convert.impl;

import com.swak.common.convert.ConvertUtil;
import com.swak.common.convert.Converter;
import com.swak.common.i18n.I18nMessageFormat;
import com.swak.common.util.DatePattern;
import com.swak.common.util.DateTimeUtils;
import com.swak.common.util.date.TemporalAccessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * DateConverter.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
@Slf4j
public class DateConverter implements Converter<Date> {

    private final Class<? extends java.util.Date> targetType;

    private static final DateTimeFormatter FDF_NORM_DATETIME_MS = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_MS_PATTERN);
    private static final DateTimeFormatter FDF_NORM_DATETIME = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);

    /**
     * 构造
     *
     * @param targetType 目标类型
     */
    public DateConverter(Class<? extends java.util.Date> targetType) {
        this.targetType = targetType;
    }


    @Override
    public Date convert(Object value, Date defaultValue,String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof TemporalAccessor) {
            return wrap(new Date(TemporalAccessorUtil.toInstant((TemporalAccessor) value).toEpochMilli()));
        }
        if (value instanceof Calendar) {
            return wrap(((Calendar) value).getTime());
        }
        if (value instanceof Number) {
            return wrap(new Date(((Number) value).longValue()));
        }
        if (value instanceof Date) {
            return wrap((Date) value);
        }
        Date wrapDateTime = wrapDateTime(ConvertUtil.toStr(value,dataFormats));
        if (Objects.nonNull(wrapDateTime)) {
            return wrap(wrapDateTime);
        }
        return defaultValue;
    }

    @Override
    public String convertToStr(Date value, String defaultValue, String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        String format = Converter.getGlobalDateFormat();
        if (ArrayUtils.isNotEmpty(dataFormats)) {
            format = dataFormats[0];
        }
        return DateTimeUtils.date2String(value, format);
    }

    public static Date wrapDateTime(String time, String... dataFormats) {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        try {
            if (ArrayUtils.isEmpty(dataFormats)) {
                if (time.length() == 13) {
                    return new Date(Long.parseLong(time));
                }
                if (time.length() == 23) {
                    return TemporalAccessorUtil.parse(time, FDF_NORM_DATETIME_MS);
                }
                if (time.length() == 19) {
                    return TemporalAccessorUtil.parse(time, FDF_NORM_DATETIME);
                }
            }
            String format = Converter.getGlobalDateFormat();
            if (ArrayUtils.isNotEmpty(dataFormats)) {
                format = dataFormats[0];
            }
            return DateTimeUtils.string2Date(time, format);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    private java.util.Date wrap(Date date) {
        // 返回指定类型
        if (java.util.Date.class == targetType) {
            return date;
        }
        if (java.sql.Date.class == targetType) {
            return new java.sql.Date(date.getTime());
        }
        if (java.sql.Time.class == targetType) {
            return new java.sql.Time(date.getTime());
        }
        if (java.sql.Timestamp.class == targetType) {
            return new Timestamp(date.getTime());
        }
        throw new UnsupportedOperationException(I18nMessageFormat.format("Unsupported target Date type: {}", this.targetType.getName()));
    }
}
