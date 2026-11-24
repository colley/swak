package com.swak.test.common.util;

import com.swak.common.util.URLBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link URLBuilder} 的拼接与编码回归。
 * <p>
 * encodeURIComponent 保留 {@code -_.!~*'()} 与字母数字，其余按 UTF-8 百分号编码（小写十六进制）。
 */
@DisplayName("URLBuilder URL 拼接")
class URLBuilderTest {

    @Test
    @DisplayName("encodeURIComponent：null、空串、保留字符、中文与空格")
    void encodeUriComponent() {
        assertThat(URLBuilder.encodeURIComponent(null)).isNull();
        assertThat(URLBuilder.encodeURIComponent("")).isEmpty();
        assertThat(URLBuilder.encodeURIComponent("aZ0-_.!~*'()")).isEqualTo("aZ0-_.!~*'()");
        assertThat(URLBuilder.encodeURIComponent("你")).isEqualTo("%e4%bd%a0");
        assertThat(URLBuilder.encodeURIComponent("a b")).isEqualTo("a%20b");
        assertThat(URLBuilder.encodeURIComponent("ab你cd")).isEqualTo("ab%e4%bd%a0cd");
    }

    @Test
    @DisplayName("appendPath + appendParam：第一个参数用 ?，后续用 &")
    void basicUrl() {
        String url = new URLBuilder()
                .appendPath("https://example.com/api")
                .appendParam("a", "1")
                .appendParam("b", "2")
                .toString();

        assertThat(url).isEqualTo("https://example.com/api?a=1&b=2");
    }

    @Test
    @DisplayName("appendParam：key 或 value 为 null 时整对被忽略")
    void nullParamIgnored() {
        String url = new URLBuilder()
                .appendPath("https://example.com")
                .appendParam(null, "1")
                .appendParam("a", null)
                .toString();

        assertThat(url).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("没有 path 时第一个参数前不加 ?")
    void paramsWithoutPath() {
        assertThat(new URLBuilder().appendParam("a", "1").appendParam("b", "2").toString())
                .isEqualTo("a=1&b=2");
    }

    @Test
    @DisplayName("appendParamEncode：value 会被编码，key 不会")
    void appendParamEncode() {
        String url = new URLBuilder()
                .appendPath("https://example.com")
                .appendParamEncode("k", "a b")
                .appendParamEncode("c", "你", "UTF-8")
                .toString();

        assertThat(url).isEqualTo("https://example.com?k=a%20b&c=%e4%bd%a0");
    }

    @Test
    @DisplayName("appendPath(null) 忽略；重复设置 path 或参数后再设 path 抛 IllegalStateException")
    void illegalPathUsage() {
        assertThat(new URLBuilder().appendPath(null).toString()).isEmpty();

        assertThatThrownBy(() -> new URLBuilder().appendPath("a").appendPath("b"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missed the trick to set path");

        assertThatThrownBy(() -> new URLBuilder().appendParam("a", "1").appendPath("b"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("appendLabel / append：原样追加")
    void appendLabelAndRaw() {
        String url = new URLBuilder()
                .appendPath("https://example.com")
                .appendParam("a", "1")
                .appendLabel("section")
                .toString();

        assertThat(url).isEqualTo("https://example.com?a=1#section");
        assertThat(new URLBuilder().append("raw").toString()).isEqualTo("raw");
    }
}
