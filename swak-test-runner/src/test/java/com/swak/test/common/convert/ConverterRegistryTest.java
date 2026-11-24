package com.swak.test.common.convert;

import com.swak.common.convert.Converter;
import com.swak.common.convert.ConverterRegistry;
import com.swak.common.exception.SwakException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ConverterRegistry} 的注册表查找与兜底逻辑回归。
 */
@DisplayName("ConverterRegistry 转换器注册表")
class ConverterRegistryTest {

    private final ConverterRegistry registry = ConverterRegistry.getInstance();

    @Test
    @DisplayName("getInstance：单例")
    void singleton() {
        assertThat(ConverterRegistry.getInstance()).isSameAs(registry);
    }

    @Test
    @DisplayName("getConverter：已注册的类型能取到，未注册的返回 null")
    void getConverter() {
        assertThat(registry.getConverter(int.class)).isNotNull();
        assertThat(registry.getConverter(Integer.class)).isNotNull();
        assertThat(registry.getConverter(String.class)).isNotNull();
        assertThat(registry.getConverter(Boolean.class)).isNotNull();
        assertThat(registry.getConverter(Date.class)).isNotNull();
        assertThat(registry.getConverter(LocalDateTime.class)).isNotNull();
        assertThat(registry.getConverter(LocalDateTime.class)).isInstanceOf(Converter.class);

        assertThat(registry.getConverter(Object.class)).isNull();
        assertThat(registry.getConverter(Thread.class)).isNull();
    }

    @Test
    @DisplayName("convertToStr：null 回退默认值，无转换器也回退默认值")
    void convertToStr() {
        assertThat(registry.convertToStr(null, "def")).isEqualTo("def");
        assertThat(registry.convertToStr(5, "def")).isEqualTo("5");
        assertThat(registry.convertToStr(Boolean.TRUE, "def")).isEqualTo("true");
        assertThat(registry.convertToStr(new Object(), "def")).isEqualTo("def");
        assertThat(registry.convertToStr(LocalDate.of(2024, 1, 2), "def", "yyyy/MM/dd"))
                .isEqualTo("2024/01/02");
    }

    @Test
    @DisplayName("convert：目标类型命中注册表")
    void convertRegisteredType() {
        assertThat(registry.convert(Integer.class, "12", 0)).isEqualTo(12);
        assertThat(registry.convert(Integer.class, 12.9d, 0)).isEqualTo(12);
        assertThat(registry.convert(Long.class, "12", 0L)).isEqualTo(12L);
        assertThat(registry.convert(String.class, 5, "def")).isEqualTo("5");
        assertThat(registry.convert(BigDecimal.class, "1.5", BigDecimal.ZERO))
                .isEqualByComparingTo("1.5");
        assertThat(registry.convert(Boolean.class, "yes", false)).isTrue();
        assertThat(registry.convert(Character.class, "abc", 'x')).isEqualTo('a');
    }

    @Test
    @DisplayName("convert：值为 null 直接返回默认值，不触发转换")
    void convertNullValue() {
        assertThat(registry.convert(Integer.class, null, 7)).isEqualTo(7);
        assertThat(registry.convert(Thread.class, null, (Object) null)).isNull();
    }

    @Test
    @DisplayName("convert：目标类型未注册时回退到默认值的类型")
    void convertFallsBackToDefaultValueType() {
        Object result = registry.convert(Object.class, "abc", "def");

        assertThat(result).isEqualTo("abc");
    }

    @Test
    @DisplayName("convert：目标类型与默认值类型都未注册时抛 SwakException")
    void convertUnsupported() {
        assertThatThrownBy(() -> registry.convert(Thread.class, "x", new Object()))
                .isInstanceOf(SwakException.class)
                .hasMessageContaining("Can not Converter from");
    }
}
