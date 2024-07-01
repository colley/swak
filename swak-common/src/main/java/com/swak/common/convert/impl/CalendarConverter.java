package com.swak.common.convert.impl;

import com.swak.common.convert.ConvertUtil;
import com.swak.common.convert.Converter;
import com.swak.common.util.DateTimeUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * CalendarConverter.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class CalendarConverter implements Converter<Calendar> {

    @Override
    public Calendar convert(Object value, Calendar defaultValue, String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof Calendar) {
            return (Calendar) value;
        }

        if (value instanceof Number) {
            final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(((Number) value).longValue());
            return cal;
        }
        if (value instanceof Date) {
            final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(((Date) value).getTime());
            return cal;
        }
        String valueStr = ConvertUtil.toStr(value, dataFormats);
        String format = Converter.getGlobalDateFormat();
        if (ArrayUtils.isNotEmpty(dataFormats)) {
            format = dataFormats[0];
        }
        return DateTimeUtils.string2Calendar(valueStr, format);
    }

    @Override
    public String convertToStr(Calendar value, String defaultValue, String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        String format = Converter.getGlobalDateFormat();
        if (ArrayUtils.isNotEmpty(dataFormats)) {
            format = dataFormats[0];
        }
        return DateTimeUtils.calendar2String(value, format);
    }
}
