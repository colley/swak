package com.swak.common.util.date;

import com.swak.common.util.DatePattern;
import com.swak.common.util.GetterUtil;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.TimeZone;


public class LocalDateTimeUtil {

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }


    public static LocalDateTime of(Instant instant) {
        return of(instant, ZoneId.systemDefault());
    }


    public static LocalDateTime ofUTC(Instant instant) {
        return of(instant, ZoneId.of("UTC"));
    }


    public static LocalDateTime of(ZonedDateTime zonedDateTime) {
        if (null == zonedDateTime) {
            return null;
        }
        return zonedDateTime.toLocalDateTime();
    }


    public static LocalDateTime of(Instant instant, ZoneId zoneId) {
        if (null == instant) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, GetterUtil.defaultIfNull(zoneId, ZoneId::systemDefault));
    }


    public static LocalDateTime of(Instant instant, TimeZone timeZone) {
        if (null == instant) {
            return null;
        }

        return of(instant, GetterUtil.defaultIfNull(timeZone, TimeZone::getDefault).toZoneId());
    }


    public static LocalDateTime of(long epochMilli) {
        return of(Instant.ofEpochMilli(epochMilli));
    }


    public static LocalDateTime ofUTC(long epochMilli) {
        return ofUTC(Instant.ofEpochMilli(epochMilli));
    }


    public static LocalDateTime of(long epochMilli, ZoneId zoneId) {
        return of(Instant.ofEpochMilli(epochMilli), zoneId);
    }


    public static LocalDateTime of(long epochMilli, TimeZone timeZone) {
        return of(Instant.ofEpochMilli(epochMilli), timeZone);
    }

    public static LocalDateTime of(Date date) {
        if (null == date) {
            return null;
        }
        return of(date.toInstant());
    }

    public static LocalDateTime of(TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        if (temporalAccessor instanceof LocalDate) {
            return ((LocalDate) temporalAccessor).atStartOfDay();
        } else if(temporalAccessor instanceof Instant){
            return LocalDateTime.ofInstant((Instant) temporalAccessor, ZoneId.systemDefault());
        }

        // issue#3301
        try{
            return LocalDateTime.from(temporalAccessor);
        } catch (final Exception ignore){
            //ignore
        }

        try{
            return ZonedDateTime.from(temporalAccessor).toLocalDateTime();
        } catch (final Exception ignore){
            //ignore
        }

        try{
            return LocalDateTime.ofInstant(Instant.from(temporalAccessor), ZoneId.systemDefault());
        } catch (final Exception ignore){
            //ignore
        }

        return LocalDateTime.of(
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.YEAR),
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.MONTH_OF_YEAR),
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.DAY_OF_MONTH),
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.HOUR_OF_DAY),
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.MINUTE_OF_HOUR),
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.SECOND_OF_MINUTE),
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.NANO_OF_SECOND)
        );
    }


    public static LocalDate ofDate(TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        if (temporalAccessor instanceof LocalDateTime) {
            return ((LocalDateTime) temporalAccessor).toLocalDate();
        } else if(temporalAccessor instanceof Instant){
            return of(temporalAccessor).toLocalDate();
        }

        return LocalDate.of(
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.YEAR),
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.MONTH_OF_YEAR),
                TemporalAccessorUtil.get(temporalAccessor, ChronoField.DAY_OF_MONTH)
        );
    }


    public static LocalDateTime parse(CharSequence text) {
        return parse(text, (DateTimeFormatter) null);
    }


    public static LocalDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        if (null == formatter) {
            return LocalDateTime.parse(text);
        }

        return of(formatter.parse(text));
    }


    public static LocalDateTime parse(CharSequence text, String format) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        DateTimeFormatter formatter = null;
        if (StringUtils.isNotBlank(format)) {
            formatter = DateTimeFormatter.ofPattern(format);
        }
        return parse(text, formatter);
    }

    public static LocalDate parseDate(CharSequence text) {
        return parseDate(text, (DateTimeFormatter) null);
    }


    public static LocalDate parseDate(CharSequence text, DateTimeFormatter formatter) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        if (null == formatter) {
            return LocalDate.parse(text);
        }

        return ofDate(formatter.parse(text));
    }


    public static LocalDate parseDate(CharSequence text, String format) {
        if (null == text) {
            return null;
        }
        return parseDate(text, DateTimeFormatter.ofPattern(format));
    }

    public static String formatNormal(LocalDateTime time) {
        return format(time, DatePattern.NORM_DATETIME_FORMATTER);
    }


    public static String format(LocalDateTime time, DateTimeFormatter formatter) {
        return TemporalAccessorUtil.format(time, formatter);
    }


    public static String format(LocalDateTime time, String format) {
        return TemporalAccessorUtil.format(time, format);
    }


    public static String formatNormal(LocalDate date) {
        return format(date, DatePattern.NORM_DATE_FORMATTER);
    }


    public static String format(LocalDate date, DateTimeFormatter formatter) {
        return TemporalAccessorUtil.format(date, formatter);
    }


    public static String format(LocalDate date, String format) {
        if (null == date) {
            return null;
        }
        return format(date, DateTimeFormatter.ofPattern(format));
    }


    public static LocalDateTime offset(LocalDateTime time, long number, TemporalUnit field) {
        return TemporalUtil.offset(time, number, field);
    }

    public static Duration between(LocalDateTime startTimeInclude, LocalDateTime endTimeExclude) {
        return TemporalUtil.between(startTimeInclude, endTimeExclude);
    }


    public static long between(LocalDateTime startTimeInclude, LocalDateTime endTimeExclude, ChronoUnit unit) {
        return TemporalUtil.between(startTimeInclude, endTimeExclude, unit);
    }


    public static Period betweenPeriod(LocalDate startTimeInclude, LocalDate endTimeExclude) {
        return Period.between(startTimeInclude, endTimeExclude);
    }


    public static LocalDateTime beginOfDay(LocalDateTime time) {
        return time.with(LocalTime.MIN);
    }

    public static LocalDateTime endOfDay(LocalDateTime time) {
        return endOfDay(time, false);
    }


    public static LocalDateTime endOfDay(LocalDateTime time, boolean truncateMillisecond) {
        if (truncateMillisecond) {
            return time.with(LocalTime.of(23, 59, 59));
        }
        return time.with(LocalTime.MAX);
    }


    public static long toEpochMilli(TemporalAccessor temporalAccessor) {
        return TemporalAccessorUtil.toEpochMilli(temporalAccessor);
    }


    public static boolean isWeekend(LocalDateTime localDateTime) {
        return isWeekend(localDateTime.toLocalDate());
    }

    public static boolean isWeekend(LocalDate localDate) {
        final DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return DayOfWeek.SATURDAY == dayOfWeek || DayOfWeek.SUNDAY == dayOfWeek;
    }
    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     * @since 5.8.5
     */
    public static boolean isSameDay(final LocalDateTime date1, final LocalDateTime date2) {
        return date1 != null && date2 != null && isSameDay(date1.toLocalDate(), date2.toLocalDate());
    }


    public static boolean isSameDay(final LocalDate date1, final LocalDate date2) {
        return date1 != null && date2 != null && date1.isEqual(date2);
    }


    public static boolean isIn(ChronoLocalDateTime<?> date, ChronoLocalDateTime<?> beginDate, ChronoLocalDateTime<?> endDate) {
        return TemporalAccessorUtil.isIn(date, beginDate, endDate);
    }


    public static boolean isIn(ChronoLocalDateTime<?> date, ChronoLocalDateTime<?> beginDate,
                               ChronoLocalDateTime<?> endDate, boolean includeBegin, boolean includeEnd) {
        return TemporalAccessorUtil.isIn(date, beginDate, endDate, includeBegin, includeEnd);
    }
}
