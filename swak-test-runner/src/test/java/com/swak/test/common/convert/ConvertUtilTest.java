package com.swak.test.common.convert;

import com.swak.common.convert.ConvertUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ConvertUtil} 的静态入口回归。
 * <p>
 * 「quietly」系列吞异常回退默认值，「convert」系列直接抛，两条路径都要钉住。
 */
@DisplayName("ConvertUtil 类型转换")
class ConvertUtilTest {

    private static final LocalDateTime FIXED = LocalDateTime.of(2024, 6, 26, 14, 56, 32);

    @Test
    @DisplayName("toStr：null、默认值、显式日期格式")
    void toStr() {
        assertThat(ConvertUtil.toStr(null)).isNull();
        assertThat(ConvertUtil.toStr(null, "def")).isEqualTo("def");
        assertThat(ConvertUtil.toStr(5)).isEqualTo("5");
        assertThat(ConvertUtil.toStr(LocalDate.of(2024, 1, 2), new String[]{"yyyy/MM/dd"}))
                .isEqualTo("2024/01/02");
        assertThat(ConvertUtil.toStr(FIXED, null, new String[]{"yyyy-MM-dd HH:mm:ss"}))
                .isEqualTo("2024-06-26 14:56:32");
    }

    @Test
    @DisplayName("toDateStr：Date 按格式输出，时间对象直接格式化，null 返回 null")
    void toDateStr() {
        assertThat(ConvertUtil.toDateStr(null)).isNull();
        assertThat(ConvertUtil.toDateStr(FIXED, "yyyy-MM-dd HH:mm:ss.SSS")).isEqualTo("2024-06-26 14:56:32.000");
        assertThat(ConvertUtil.toDateStr(LocalDate.of(2024, 1, 2), "yyyy/MM/dd")).isEqualTo("2024/01/02");

        String fromDate = ConvertUtil.toDateStr(new Date(0L), "yyyy-MM-dd HH:mm:ss");
        assertThat(fromDate).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("toChar：布尔转码位，字符串取首字符，空串返回 null")
    void toChar() {
        assertThat(ConvertUtil.toChar(Boolean.TRUE)).isEqualTo((char) 1);
        assertThat(ConvertUtil.toChar(Boolean.FALSE)).isEqualTo((char) 0);
        assertThat(ConvertUtil.toChar("abc")).isEqualTo('a');
        assertThat(ConvertUtil.toChar("")).isNull();
        assertThat(ConvertUtil.toChar(null)).isNull();
    }

    @Test
    @DisplayName("toByte / toShort：合法、非法与默认值")
    void toByteAndShort() {
        assertThat(ConvertUtil.toByte("12")).isEqualTo((byte) 12);
        assertThat(ConvertUtil.toByte("x")).isNull();
        assertThat(ConvertUtil.toByte("x", (byte) 3)).isEqualTo((byte) 3);

        assertThat(ConvertUtil.toShort("12")).isEqualTo((short) 12);
        assertThat(ConvertUtil.toShort("x")).isNull();
        assertThat(ConvertUtil.toShort("x", (short) 3)).isEqualTo((short) 3);
    }

    @Test
    @DisplayName("toNumber / toInt：小数截断、非法回退")
    void toNumberAndInt() {
        assertThat(ConvertUtil.toNumber("12")).isEqualTo(12);
        assertThat(ConvertUtil.toNumber("x")).isNull();
        assertThat(ConvertUtil.toNumber("x", 9)).isEqualTo(9);

        assertThat(ConvertUtil.toInt("5.01")).isEqualTo(5);
        assertThat(ConvertUtil.toInt(5.99d)).isEqualTo(5);
        assertThat(ConvertUtil.toInt("abc")).isNull();
        assertThat(ConvertUtil.toInt("abc", 7)).isEqualTo(7);
    }

    @Test
    @DisplayName("toLong / toDouble / toFloat")
    void toLongDoubleFloat() {
        assertThat(ConvertUtil.toLong("12")).isEqualTo(12L);
        assertThat(ConvertUtil.toLong("abc")).isNull();
        assertThat(ConvertUtil.toLong("abc", 3L)).isEqualTo(3L);

        assertThat(ConvertUtil.toDouble("1.5")).isEqualTo(1.5d);
        assertThat(ConvertUtil.toDouble("x")).isNull();
        assertThat(ConvertUtil.toDouble("x", 1.1d)).isEqualTo(1.1d);

        assertThat(ConvertUtil.toFloat("1.5")).isEqualTo(1.5f);
        assertThat(ConvertUtil.toFloat("x")).isNull();
        assertThat(ConvertUtil.toFloat("x", 1.1f)).isEqualTo(1.1f);
    }

    @Test
    @DisplayName("toBool：数字 0 为 false、非 0 为 true，字符串走布尔词表")
    void toBool() {
        assertThat(ConvertUtil.toBool("yes")).isTrue();
        assertThat(ConvertUtil.toBool("off")).isFalse();
        assertThat(ConvertUtil.toBool(0)).isFalse();
        assertThat(ConvertUtil.toBool(3)).isTrue();
        assertThat(ConvertUtil.toBool("unknown")).isNull();
        assertThat(ConvertUtil.toBool("unknown", true)).isTrue();
    }

    @Test
    @DisplayName("toBigInteger / toBigDecimal")
    void toBigNumbers() {
        assertThat(ConvertUtil.toBigInteger("12")).isEqualTo(new BigInteger("12"));
        assertThat(ConvertUtil.toBigInteger("x")).isNull();
        assertThat(ConvertUtil.toBigInteger("x", BigInteger.ONE)).isEqualTo(BigInteger.ONE);

        assertThat(ConvertUtil.toBigDecimal("1.5")).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(ConvertUtil.toBigDecimal("x")).isNull();
        assertThat(ConvertUtil.toBigDecimal("x", BigDecimal.TEN)).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    @DisplayName("toLocalDateTime / toDate：时间对象互转")
    void temporalConversions() {
        assertThat(ConvertUtil.toLocalDateTime(LocalDate.of(2024, 1, 2)))
                .isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
        assertThat(ConvertUtil.toLocalDateTime(FIXED)).isEqualTo(FIXED);
        assertThat(ConvertUtil.toLocalDateTime(null)).isNull();

        Date date = ConvertUtil.toDate(FIXED);
        assertThat(date.getTime()).isEqualTo(FIXED.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        assertThat(ConvertUtil.toDate(0L)).isEqualTo(new Date(0L));
        assertThat(ConvertUtil.toDate(new Date(5L))).isEqualTo(new Date(5L));
        assertThat(ConvertUtil.toDate("bad")).isNull();
        assertThat(ConvertUtil.toDate("bad", new Date(1L))).isEqualTo(new Date(1L));
        assertThat(ConvertUtil.toDate(null)).isNull();
    }

    @Test
    @DisplayName("toDate：带显式格式的字符串解析")
    void toDateWithFormat() {
        Date parsed = ConvertUtil.toDate("2024-06-26 14:56:32", "yyyy-MM-dd HH:mm:ss");

        assertThat(parsed).isNotNull();
        assertThat(ConvertUtil.toLocalDateTime(parsed)).isEqualTo(FIXED);
        assertThat(ConvertUtil.toDateStr(parsed, "yyyy-MM-dd HH:mm:ss")).isEqualTo("2024-06-26 14:56:32");
    }

    @Test
    @DisplayName("convert：类型转换成功与失败（非 quiet 直接抛异常）")
    void convert() {
        assertThat(ConvertUtil.convert(Integer.class, "12")).isEqualTo(12);
        assertThat(ConvertUtil.convert(LocalDate.class, FIXED)).isEqualTo(LocalDate.of(2024, 6, 26));
        assertThat(ConvertUtil.convert(Instant.class, FIXED))
                .isEqualTo(FIXED.atZone(ZoneId.systemDefault()).toInstant());

        assertThatThrownBy(() -> ConvertUtil.convert(Integer.class, "abc"))
                .isInstanceOf(NumberFormatException.class);
        assertThat(ConvertUtil.convertQuietly(Integer.class, "abc", 5)).isEqualTo(5);
        assertThat(ConvertUtil.convertQuietly(Integer.class, "12", 5)).isEqualTo(12);
    }

    @Test
    @DisplayName("convert：Type 入口与带默认值的重载")
    void convertOverloads() {
        Type integerType = Integer.class;

        assertThat(ConvertUtil.<Integer>convert(integerType, "13")).isEqualTo(13);
        assertThat(ConvertUtil.convert(Integer.class, "14", 0)).isEqualTo(14);
        assertThat(ConvertUtil.<Integer>convert(integerType, "15", 0, "yyyy")).isEqualTo(15);
        assertThat(ConvertUtil.toStr(5, "ignored-default")).isEqualTo("5");
        assertThat(ConvertUtil.toChar(null, 'x')).as("值为 null 时由注册表直接返回默认值").isEqualTo('x');
    }
}
