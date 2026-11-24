package com.swak.test.common.convert;

import com.swak.common.convert.FormatRegisterFactory;
import com.swak.common.convert.impl.TemporalAccessorConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.Era;
import java.time.chrono.IsoEra;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TemporalAccessorConverter} 的 java.time 互转回归。
 * <p>
 * 字符串入口依赖全局日期格式（{@code FormatRegisterFactory} 未注册时全局格式为 null，
 * 会直接 NPE），且实现内部用 {@code Instant::from} 解析，格式必须带时区偏移，
 * 因此这里注册一个带 {@code XXX} 的格式后再测字符串入口。
 */
@DisplayName("TemporalAccessorConverter 时间转换器")
class TemporalAccessorConverterTest {

    private static final String GLOBAL_FORMAT = "yyyy-MM-dd HH:mm:ssXXX";

    private static final LocalDateTime FIXED = LocalDateTime.of(2024, 1, 2, 3, 4, 5);

    /** 与 GLOBAL_FORMAT 对应、带 +08:00 偏移的文本 */
    private static final String ZONED_TEXT = "2024-01-02 03:04:05+08:00";

    @BeforeAll
    static void registerGlobalDateFormat() {
        FormatRegisterFactory.registerDateFormat(GLOBAL_FORMAT);
    }

    private static TemporalAccessor convert(TemporalAccessorConverter converter, Object value) {
        return converter.convert(value, null);
    }

    @Test
    @DisplayName("convert：null 返回默认值")
    void nullValue() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(LocalDateTime.class);

        assertThat(converter.convert(null, null)).isNull();
        assertThat(converter.convert(null, FIXED)).isEqualTo(FIXED);
    }

    @Test
    @DisplayName("convert(String)：按全局格式解析，偏移换算到系统默认时区")
    void fromCharSequence() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(LocalDateTime.class);
        LocalDateTime expected = LocalDateTime.ofInstant(
                Instant.parse("2024-01-01T19:04:05Z"), ZoneId.systemDefault());

        assertThat(convert(converter, ZONED_TEXT)).isEqualTo(expected);
        assertThat(convert(converter, "   ")).isNull();
    }

    @Test
    @DisplayName("convert(Number)：按毫秒时间戳解析")
    void fromNumber() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(LocalDateTime.class);
        long millis = 1700000000000L;

        assertThat(convert(converter, millis))
                .isEqualTo(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
    }

    @Test
    @DisplayName("convert(Date/Calendar)：按各自时区解析")
    void fromDateAndCalendar() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(LocalDateTime.class);

        assertThat(convert(converter, new Date(0L)))
                .isEqualTo(LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()));

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(0L);
        assertThat(convert(converter, calendar)).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("convert(TemporalAccessor)：LocalDateTime / ZonedDateTime / LocalDate 归一")
    void fromTemporalAccessor() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(LocalDateTime.class);

        assertThat(convert(converter, FIXED)).isEqualTo(FIXED);
        assertThat(convert(converter, ZonedDateTime.of(FIXED, ZoneId.of("UTC")))).isEqualTo(FIXED);
        assertThat(convert(converter, LocalDate.of(2024, 1, 2))).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
    }

    @Test
    @DisplayName("目标 Instant / LocalDate / LocalTime / ZonedDateTime / OffsetDateTime / OffsetTime")
    void otherTargetTypes() {
        Instant expectedInstant = FIXED.atZone(ZoneId.systemDefault()).toInstant();

        assertThat(convert(new TemporalAccessorConverter(Instant.class), FIXED)).isEqualTo(expectedInstant);
        assertThat(convert(new TemporalAccessorConverter(LocalDate.class), FIXED)).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(convert(new TemporalAccessorConverter(LocalTime.class), FIXED)).isEqualTo(LocalTime.of(3, 4, 5));
        assertThat(convert(new TemporalAccessorConverter(ZonedDateTime.class), FIXED))
                .isEqualTo(FIXED.atZone(ZoneId.systemDefault()));
        assertThat(convert(new TemporalAccessorConverter(OffsetDateTime.class), FIXED))
                .isEqualTo(FIXED.atZone(ZoneId.systemDefault()).toOffsetDateTime());
        assertThat(convert(new TemporalAccessorConverter(OffsetTime.class), FIXED))
                .isEqualTo(FIXED.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime());

        // 从 ZonedDateTime 出发的各分支
        ZonedDateTime zoned = ZonedDateTime.of(FIXED, ZoneId.of("UTC"));
        assertThat(convert(new TemporalAccessorConverter(Instant.class), zoned)).isEqualTo(zoned.toInstant());
        assertThat(convert(new TemporalAccessorConverter(LocalDateTime.class), zoned)).isEqualTo(FIXED);
        assertThat(convert(new TemporalAccessorConverter(LocalDate.class), zoned)).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(convert(new TemporalAccessorConverter(LocalTime.class), zoned)).isEqualTo(LocalTime.of(3, 4, 5));
        assertThat(convert(new TemporalAccessorConverter(OffsetDateTime.class), zoned)).isEqualTo(zoned.toOffsetDateTime());
        assertThat(convert(new TemporalAccessorConverter(OffsetTime.class), zoned)).isEqualTo(zoned.toOffsetDateTime().toOffsetTime());

        // 从 Instant 出发的各分支
        assertThat(convert(new TemporalAccessorConverter(Instant.class), Instant.EPOCH)).isEqualTo(Instant.EPOCH);
        assertThat(convert(new TemporalAccessorConverter(LocalDate.class), Instant.EPOCH))
                .isEqualTo(LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()).toLocalDate());
        assertThat(convert(new TemporalAccessorConverter(LocalTime.class), Instant.EPOCH))
                .isEqualTo(LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()).toLocalTime());
        assertThat(convert(new TemporalAccessorConverter(ZonedDateTime.class), Instant.EPOCH))
                .isEqualTo(Instant.EPOCH.atZone(ZoneId.systemDefault()));
    }

    @Test
    @DisplayName("DayOfWeek：字符串、数字、时间三种入参")
    void dayOfWeek() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(DayOfWeek.class);

        assertThat(convert(converter, "MONDAY")).isEqualTo(DayOfWeek.MONDAY);
        assertThat(convert(converter, 2)).isEqualTo(DayOfWeek.TUESDAY);
        // 2024-01-02 是周二
        assertThat(convert(converter, LocalDate.of(2024, 1, 2))).isEqualTo(DayOfWeek.TUESDAY);
    }

    @Test
    @DisplayName("Month：字符串、数字、时间三种入参")
    void month() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(Month.class);

        assertThat(convert(converter, "MARCH")).isEqualTo(Month.MARCH);
        assertThat(convert(converter, 3)).isEqualTo(Month.MARCH);
        assertThat(convert(converter, LocalDate.of(2024, 1, 2))).isEqualTo(Month.JANUARY);
    }

    @Test
    @DisplayName("Era：字符串与数字都映射到 IsoEra")
    void era() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(Era.class);

        assertThat(convert(converter, "BCE")).isEqualTo(IsoEra.BCE);
        assertThat(convert(converter, 1)).isEqualTo(IsoEra.CE);
    }

    @Test
    @DisplayName("MonthDay：字符串与时间入参")
    void monthDay() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(MonthDay.class);

        assertThat(convert(converter, "--01-02")).isEqualTo(MonthDay.of(1, 2));
        assertThat(convert(converter, LocalDate.of(2024, 1, 2))).isEqualTo(MonthDay.of(1, 2));
    }

    @Test
    @DisplayName("未覆盖的目标类型返回 null")
    void unsupportedTargetReturnsNull() {
        assertThat(convert(new TemporalAccessorConverter(Year.class), Instant.EPOCH)).isNull();
    }

    @Test
    @DisplayName("convertToStr：null 回退默认值，显式格式优先，否则用全局格式")
    void convertToStr() {
        TemporalAccessorConverter converter = new TemporalAccessorConverter(LocalDateTime.class);
        ZonedDateTime zoned = ZonedDateTime.of(FIXED, ZoneId.of("+08:00"));

        assertThat(converter.convertToStr(null, "def")).isEqualTo("def");
        assertThat(converter.convertToStr(FIXED, "def", "yyyy/MM/dd HH:mm:ss"))
                .isEqualTo("2024/01/02 03:04:05");
        assertThat(converter.convertToStr(zoned, "def")).isEqualTo("2024-01-02 03:04:05+08:00");
    }
}
