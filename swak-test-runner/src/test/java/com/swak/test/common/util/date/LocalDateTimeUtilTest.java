package com.swak.test.common.util.date;

import com.swak.common.util.date.LocalDateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link LocalDateTimeUtil} 的转换/解析/区间回归。
 * <p>
 * 全部使用固定时间戳，不依赖当前时间；涉及 systemDefault 的断言只在
 * 「两种算法互相一致」的层面比较，避免在 CI 不同时区下翻转。
 */
@DisplayName("LocalDateTimeUtil 时间工具")
class LocalDateTimeUtilTest {

    private static final LocalDateTime FIXED = LocalDateTime.of(2024, 1, 2, 3, 4, 5);

    @Test
    @DisplayName("now()：非空")
    void now() {
        assertThat(LocalDateTimeUtil.now()).isNotNull();
    }

    @Test
    @DisplayName("of(Instant)：UTC 与指定时区换算")
    void ofInstant() {
        assertThat(LocalDateTimeUtil.ofUTC(Instant.EPOCH)).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
        assertThat(LocalDateTimeUtil.of(Instant.EPOCH, ZoneId.of("UTC")))
                .isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
        assertThat(LocalDateTimeUtil.of((Instant) null)).isNull();
    }

    @Test
    @DisplayName("of(Instant, ZoneId/TimeZone)：时区为 null 时回落系统默认")
    void ofInstantWithZone() {
        assertThat(LocalDateTimeUtil.of(Instant.EPOCH, (ZoneId) null))
                .isEqualTo(LocalDateTimeUtil.of(Instant.EPOCH, ZoneId.systemDefault()));
        assertThat(LocalDateTimeUtil.of(Instant.EPOCH, (TimeZone) null))
                .isEqualTo(LocalDateTimeUtil.of(Instant.EPOCH, ZoneId.systemDefault()));
        assertThat(LocalDateTimeUtil.of(Instant.EPOCH, TimeZone.getTimeZone("UTC")))
                .isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
        assertThat(LocalDateTimeUtil.of((Instant) null, TimeZone.getTimeZone("UTC"))).isNull();
    }

    @Test
    @DisplayName("of(long)：毫秒时间戳")
    void ofEpochMilli() {
        assertThat(LocalDateTimeUtil.ofUTC(0L)).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
        assertThat(LocalDateTimeUtil.of(0L, ZoneId.of("UTC"))).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
        assertThat(LocalDateTimeUtil.of(0L, TimeZone.getTimeZone("UTC")))
                .isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
        assertThat(LocalDateTimeUtil.of(0L)).isEqualTo(LocalDateTimeUtil.of(Instant.EPOCH));
    }

    @Test
    @DisplayName("of(ZonedDateTime/Date/TemporalAccessor)：各种入参")
    void ofOtherTemporalTypes() {
        assertThat(LocalDateTimeUtil.of((ZonedDateTime) null)).isNull();
        assertThat(LocalDateTimeUtil.of(ZonedDateTime.of(FIXED, ZoneId.of("UTC")))).isEqualTo(FIXED);

        assertThat(LocalDateTimeUtil.of((Date) null)).isNull();
        assertThat(LocalDateTimeUtil.of(Date.from(Instant.EPOCH)))
                .isEqualTo(LocalDateTimeUtil.of(Instant.EPOCH));

        assertThat(LocalDateTimeUtil.of((java.time.temporal.TemporalAccessor) null)).isNull();
        assertThat(LocalDateTimeUtil.of(LocalDate.of(2024, 1, 2))).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
        assertThat(LocalDateTimeUtil.of(Instant.EPOCH)).isEqualTo(LocalDateTimeUtil.of(Instant.EPOCH, ZoneId.systemDefault()));
        assertThat(LocalDateTimeUtil.of(FIXED)).isEqualTo(FIXED);

        // 只有年字段的 Year 走最后的兜底分支，月/日取字段最小值
        assertThat(LocalDateTimeUtil.of(Year.of(2020))).isEqualTo(LocalDateTime.of(2020, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("ofDate：LocalDateTime / Instant / 其它 TemporalAccessor")
    void ofDate() {
        assertThat(LocalDateTimeUtil.ofDate(null)).isNull();
        assertThat(LocalDateTimeUtil.ofDate(FIXED)).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(LocalDateTimeUtil.ofDate(Instant.EPOCH))
                .isEqualTo(LocalDateTimeUtil.of(Instant.EPOCH).toLocalDate());
        assertThat(LocalDateTimeUtil.ofDate(LocalDate.of(2024, 5, 6))).isEqualTo(LocalDate.of(2024, 5, 6));
    }

    @Test
    @DisplayName("parse：ISO 默认格式、自定义 formatter、自定义 pattern、空白输入")
    void parse() {
        assertThat(LocalDateTimeUtil.parse("2024-01-02T03:04:05")).isEqualTo(FIXED);
        assertThat(LocalDateTimeUtil.parse("2024/01/02 03:04:05",
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))).isEqualTo(FIXED);
        assertThat(LocalDateTimeUtil.parse("2024-01-02 03:04:05", "yyyy-MM-dd HH:mm:ss")).isEqualTo(FIXED);
        assertThat(LocalDateTimeUtil.parse("2024-01-02T03:04:05", (String) null)).isEqualTo(FIXED);

        assertThat(LocalDateTimeUtil.parse("", "yyyy-MM-dd")).isNull();
        assertThat(LocalDateTimeUtil.parse((String) null)).isNull();
    }

    @Test
    @DisplayName("parseDate：默认、formatter、pattern、null 文本")
    void parseDate() {
        assertThat(LocalDateTimeUtil.parseDate("2024-01-02")).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(LocalDateTimeUtil.parseDate("2024/01/02", DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                .isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(LocalDateTimeUtil.parseDate("2024/01/02", "yyyy/MM/dd")).isEqualTo(LocalDate.of(2024, 1, 2));
        // 带时间部分也能只取日期
        assertThat(LocalDateTimeUtil.parseDate("2024-01-02 03:04:05",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).isEqualTo(LocalDate.of(2024, 1, 2));

        assertThat(LocalDateTimeUtil.parseDate("", "yyyy-MM-dd")).isNull();
        assertThat(LocalDateTimeUtil.parseDate(null, "yyyy-MM-dd")).isNull();
    }

    @Test
    @DisplayName("format：LocalDateTime / LocalDate 的默认与自定义格式")
    void format() {
        assertThat(LocalDateTimeUtil.formatNormal(FIXED)).isEqualTo("2024-01-02 03:04:05");
        assertThat(LocalDateTimeUtil.format(FIXED, "yyyy/MM/dd HH-mm-ss")).isEqualTo("2024/01/02 03-04-05");
        assertThat(LocalDateTimeUtil.format(FIXED, DateTimeFormatter.ofPattern("yyyyMMdd"))).isEqualTo("20240102");

        LocalDate date = LocalDate.of(2024, 1, 2);
        assertThat(LocalDateTimeUtil.formatNormal(date)).isEqualTo("2024-01-02");
        assertThat(LocalDateTimeUtil.format(date, "yyyy年MM月dd日")).isEqualTo("2024年01月02日");
        assertThat(LocalDateTimeUtil.format(date, DateTimeFormatter.ofPattern("yyyyMMdd"))).isEqualTo("20240102");

        assertThat(LocalDateTimeUtil.format((LocalDate) null, "yyyy")).isNull();
        assertThat(LocalDateTimeUtil.format((LocalDateTime) null, "yyyy")).isNull();
    }

    @Test
    @DisplayName("offset / between / betweenPeriod")
    void offsetAndBetween() {
        assertThat(LocalDateTimeUtil.offset(FIXED, 1, ChronoUnit.DAYS)).isEqualTo(FIXED.plusDays(1));
        assertThat(LocalDateTimeUtil.offset(null, 1, ChronoUnit.DAYS)).isNull();

        LocalDateTime end = FIXED.plusDays(1).plusHours(2);
        assertThat(LocalDateTimeUtil.between(FIXED, end)).isEqualTo(Duration.ofHours(26));
        assertThat(LocalDateTimeUtil.between(FIXED, end, ChronoUnit.HOURS)).isEqualTo(26);
        assertThat(LocalDateTimeUtil.betweenPeriod(LocalDate.of(2024, 1, 2), LocalDate.of(2024, 2, 3)))
                .isEqualTo(Period.ofMonths(1).plusDays(1));
    }

    @Test
    @DisplayName("beginOfDay / endOfDay：秒级截断与完整精度")
    void dayBoundaries() {
        assertThat(LocalDateTimeUtil.beginOfDay(FIXED)).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
        assertThat(LocalDateTimeUtil.endOfDay(FIXED))
                .isEqualTo(LocalDateTime.of(2024, 1, 2, 23, 59, 59, 999_999_999));
        assertThat(LocalDateTimeUtil.endOfDay(FIXED, true)).isEqualTo(LocalDateTime.of(2024, 1, 2, 23, 59, 59));
    }

    @Test
    @DisplayName("toEpochMilli：与系统默认时区的换算一致")
    void toEpochMilli() {
        long expected = FIXED.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        assertThat(LocalDateTimeUtil.toEpochMilli(FIXED)).isEqualTo(expected);
    }

    @Test
    @DisplayName("isWeekend / isSameDay")
    void weekendAndSameDay() {
        // 2024-06-01 是周六，2024-06-03 是周一
        assertThat(LocalDateTimeUtil.isWeekend(LocalDate.of(2024, 6, 1))).isTrue();
        assertThat(LocalDateTimeUtil.isWeekend(LocalDate.of(2024, 6, 2))).isTrue();
        assertThat(LocalDateTimeUtil.isWeekend(LocalDate.of(2024, 6, 3))).isFalse();
        assertThat(LocalDateTimeUtil.isWeekend(LocalDateTime.of(2024, 6, 1, 10, 0))).isTrue();

        assertThat(LocalDateTimeUtil.isSameDay(FIXED, FIXED.with(LocalTime.MIDNIGHT))).isTrue();
        assertThat(LocalDateTimeUtil.isSameDay(FIXED, FIXED.plusDays(1))).isFalse();
        assertThat(LocalDateTimeUtil.isSameDay((LocalDateTime) null, FIXED)).isFalse();
        assertThat(LocalDateTimeUtil.isSameDay(FIXED, (LocalDateTime) null)).isFalse();
        assertThat(LocalDateTimeUtil.isSameDay(LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 2))).isTrue();
        assertThat(LocalDateTimeUtil.isSameDay((LocalDate) null, LocalDate.of(2024, 1, 2))).isFalse();
    }

    @Test
    @DisplayName("isIn：默认包含两端，可关闭边界并支持反向区间")
    void isIn() {
        LocalDateTime begin = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 3, 0, 0);

        assertThat(LocalDateTimeUtil.isIn(begin.plusDays(1), begin, end)).isTrue();
        assertThat(LocalDateTimeUtil.isIn(begin, begin, end)).isTrue();
        assertThat(LocalDateTimeUtil.isIn(end, begin, end)).isTrue();
        assertThat(LocalDateTimeUtil.isIn(begin.minusSeconds(1), begin, end)).isFalse();

        assertThat(LocalDateTimeUtil.isIn(begin, begin, end, false, true)).isFalse();
        assertThat(LocalDateTimeUtil.isIn(end, begin, end, true, false)).isFalse();

        assertThatThrownBy(() -> LocalDateTimeUtil.isIn(null, begin, end))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Calendar 与固定时间互相换算（不走真实时钟）")
    void calendarInterop() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(0L);

        assertThat(calendar.toInstant()).isEqualTo(Instant.EPOCH);
        assertThat(LocalDateTimeUtil.of(calendar.toInstant(), ZoneId.of("UTC")))
                .isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
        assertThat(LocalDateTimeUtil.of(OffsetDateTime.of(FIXED, java.time.ZoneOffset.UTC))).isEqualTo(FIXED);
    }
}
