package com.swak.common.convert.impl;


import com.swak.common.convert.ConvertUtil;
import com.swak.common.convert.Converter;
import com.swak.common.util.DatePattern;
import com.swak.common.util.GetterUtil;
import com.swak.common.util.date.TemporalAccessorUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.chrono.Era;
import java.time.chrono.IsoEra;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class TemporalAccessorConverter implements Converter<TemporalAccessor> {

    private final Class<? extends TemporalAccessor> targetType;

    /**
     * 构造
     *
     * @param targetType 目标类型
     */
    public TemporalAccessorConverter(Class<? extends TemporalAccessor> targetType) {
        this.targetType = targetType;
    }

    @Override
    public TemporalAccessor convert(Object value, TemporalAccessor defaultValue, String... dataFormats) {
        if(Objects.isNull(value)){
            return defaultValue;
        }
        if (value instanceof Number) {
            return parseFromLong(((Number) value).longValue());
        }
        if (value instanceof TemporalAccessor) {
            return parseFromTemporalAccessor((TemporalAccessor) value);
        }
        if (value instanceof Date) {
            return parseFromInstant(((Date) value).toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof Calendar) {
            final Calendar calendar = (Calendar) value;
            return parseFromInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
        }
        return parseFromCharSequence(ConvertUtil.toStr(value, dataFormats), dataFormats);
    }

    @Override
    public String convertToStr(TemporalAccessor value, String defaultValue, String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(resolveFormat(dataFormats));
        return formatter.format(value);
    }

    /**
     * 解析使用的日期格式。
     * <p>
     * {@code Converter#convert(Object, Object)} 这类便捷入口会把 {@code dataFormats} 传成 {@code [null]}，
     * 因此不能只看数组是否非空，还要判断首个元素是否为空白，否则会拿 null 去建 {@link DateTimeFormatter}。
     */
    private String resolveFormat(String... dataFormats) {
        String format = Converter.getGlobalDateFormat();
        if (ArrayUtils.isNotEmpty(dataFormats) && StringUtils.isNotBlank(dataFormats[0])) {
            format = dataFormats[0];
        }
        if (StringUtils.isBlank(format)) {
            throw new IllegalArgumentException(
                    "未指定日期格式，且没有通过 FormatRegisterFactory 注册全局格式");
        }
        return format;
    }

    /**
     * 通过反射从字符串转java.time中的对象
     *
     * @param value 字符串值
     * @return 日期对象
     */
    private TemporalAccessor parseFromCharSequence(CharSequence value,String... dataFormats) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (DayOfWeek.class.equals(this.targetType)) {
            return DayOfWeek.valueOf(value.toString());
        } else if (Month.class.equals(this.targetType)) {
            return Month.valueOf(value.toString());
        } else if (Era.class.equals(this.targetType)) {
            return IsoEra.valueOf(value.toString());
        } else if (MonthDay.class.equals(this.targetType)) {
            return MonthDay.parse(value);
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(resolveFormat(dataFormats));
        // 不能只用 Instant::from：它要求文本里带时区或偏移，yyyy-MM-dd HH:mm:ss 这类最常见格式会直接抛
        // DateTimeException。按「带时区 → 带偏移 → 本地日期时间 → 纯日期 → 纯时间」依次尝试，
        // 缺时区信息时由后续转换按系统默认时区补齐。
        TemporalAccessor parsed = formatter.parseBest(value,
                ZonedDateTime::from, OffsetDateTime::from, LocalDateTime::from,
                LocalDate::from, LocalTime::from);
        return parseFromTemporalAccessor(parsed);
    }

    /**
     * 将Long型时间戳转换为java.time中的对象
     *
     * @param time 时间戳
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromLong(Long time) {
        if (DayOfWeek.class.equals(this.targetType)) {
            return DayOfWeek.of(Math.toIntExact(time));
        } else if (Month.class.equals(this.targetType)) {
            return Month.of(Math.toIntExact(time));
        } else if (Era.class.equals(this.targetType)) {
            return IsoEra.of(Math.toIntExact(time));
        }
        Instant instant = Instant.ofEpochMilli(time);
        return parseFromInstant(instant, null);
    }

    /**
     * 将TemporalAccessor型时间戳转换为java.time中的对象
     *
     * @param temporalAccessor TemporalAccessor对象
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromTemporalAccessor(TemporalAccessor temporalAccessor) {
        if (DayOfWeek.class.equals(this.targetType)) {
            return DayOfWeek.from(temporalAccessor);
        } else if (Month.class.equals(this.targetType)) {
            return Month.from(temporalAccessor);
        } else if (MonthDay.class.equals(this.targetType)) {
            return MonthDay.from(temporalAccessor);
        }

        TemporalAccessor result = null;
        if (temporalAccessor instanceof LocalDateTime) {
            result = parseFromLocalDateTime((LocalDateTime) temporalAccessor);
        } else if (temporalAccessor instanceof ZonedDateTime) {
            result = parseFromZonedDateTime((ZonedDateTime) temporalAccessor);
        }

        if (null == result) {
            result = parseFromInstant(TemporalAccessorUtil.toInstant(temporalAccessor), null);
        }

        return result;
    }

    /**
     * 将TemporalAccessor型时间戳转换为java.time中的对象
     *
     * @param localDateTime {@link LocalDateTime}对象
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromLocalDateTime(LocalDateTime localDateTime) {
        if (Instant.class.equals(this.targetType)) {
            return TemporalAccessorUtil.toInstant(localDateTime);
        }
        if (LocalDate.class.equals(this.targetType)) {
            return localDateTime.toLocalDate();
        }
        if (LocalTime.class.equals(this.targetType)) {
            return localDateTime.toLocalTime();
        }
        if (ZonedDateTime.class.equals(this.targetType)) {
            return localDateTime.atZone(ZoneId.systemDefault());
        }
        if (OffsetDateTime.class.equals(this.targetType)) {
            return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        if (OffsetTime.class.equals(this.targetType)) {
            return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        }

        return null;
    }

    /**
     * 将TemporalAccessor型时间戳转换为java.time中的对象
     *
     * @param zonedDateTime {@link ZonedDateTime}对象
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromZonedDateTime(ZonedDateTime zonedDateTime) {
        if (Instant.class.equals(this.targetType)) {
            return TemporalAccessorUtil.toInstant(zonedDateTime);
        }
        if (LocalDateTime.class.equals(this.targetType)) {
            return zonedDateTime.toLocalDateTime();
        }
        if (LocalDate.class.equals(this.targetType)) {
            return zonedDateTime.toLocalDate();
        }
        if (LocalTime.class.equals(this.targetType)) {
            return zonedDateTime.toLocalTime();
        }
        if (OffsetDateTime.class.equals(this.targetType)) {
            return zonedDateTime.toOffsetDateTime();
        }
        if (OffsetTime.class.equals(this.targetType)) {
            return zonedDateTime.toOffsetDateTime().toOffsetTime();
        }

        return null;
    }

    /**
     * 将TemporalAccessor型时间戳转换为java.time中的对象
     *
     * @param instant {@link Instant}对象
     * @param zoneId  时区ID，null表示当前系统默认的时区
     * @return java.time中的对象
     */
    private TemporalAccessor parseFromInstant(Instant instant, ZoneId zoneId) {
        if (Instant.class.equals(this.targetType)) {
            return instant;
        }

        zoneId = GetterUtil.defaultIfNull(zoneId, ZoneId::systemDefault);
        TemporalAccessor result = null;
        if (LocalDateTime.class.equals(this.targetType)) {
            result = LocalDateTime.ofInstant(instant, zoneId);
        } else if (LocalDate.class.equals(this.targetType)) {
            result = instant.atZone(zoneId).toLocalDate();
        } else if (LocalTime.class.equals(this.targetType)) {
            result = instant.atZone(zoneId).toLocalTime();
        } else if (ZonedDateTime.class.equals(this.targetType)) {
            result = instant.atZone(zoneId);
        } else if (OffsetDateTime.class.equals(this.targetType)) {
            result = OffsetDateTime.ofInstant(instant, zoneId);
        } else if (OffsetTime.class.equals(this.targetType)) {
            result = OffsetTime.ofInstant(instant, zoneId);
        }
        return result;
    }
}
