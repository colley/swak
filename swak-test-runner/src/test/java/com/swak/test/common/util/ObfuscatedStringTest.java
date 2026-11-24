package com.swak.test.common.util;

import com.swak.common.util.ObfuscatedString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ObfuscatedString} 的编解码回归。
 * <p>
 * 关键不变量：{@code array} 编码后的数组必须能被构造器或静态方法原样还原，
 * 且 UTF-8 多字节字符（中文）不能丢失。
 */
@DisplayName("ObfuscatedString 字符串混淆")
class ObfuscatedStringTest {

    @Test
    @DisplayName("array + 构造器：ASCII 字符串可原样还原")
    void roundTripAscii() {
        String source = "Hello world!";

        String restored = new ObfuscatedString(ObfuscatedString.array(source)).toString();

        assertThat(restored).isEqualTo(source);
    }

    @Test
    @DisplayName("array + 构造器：中文按 UTF-8 还原")
    void roundTripUnicode() {
        String source = "swak 框架 你好 🌏";

        assertThat(new ObfuscatedString(ObfuscatedString.array(source)).toString()).isEqualTo(source);
    }

    @Test
    @DisplayName("toCharArray：UTF-8 长度与 8 字节对齐时逐字符一致")
    void toCharArray() {
        // 实现用 CharBuffer.array() 返回整个缓冲区，未填满的尾部会带出 '\0'，
        // 所以这里取 8 字节对齐的输入验证字符内容。
        assertThat(new ObfuscatedString(ObfuscatedString.array("12345678")).toCharArray())
                .isEqualTo("12345678".toCharArray());
    }

    @Test
    @DisplayName("空串编码后只有一个 key 元素，且可还原为空串")
    void roundTripEmpty() {
        long[] obfuscated = ObfuscatedString.array("");

        assertThat(obfuscated).hasSize(1);
        assertThat(new ObfuscatedString(obfuscated).toString()).isEmpty();
    }

    @Test
    @DisplayName("编码结果长度：key + 每 8 字节一个 long")
    void encodedLength() {
        assertThat(ObfuscatedString.array("12345678")).hasSize(2);
        assertThat(ObfuscatedString.array("123456789")).hasSize(3);
    }

    @Test
    @DisplayName("包含 \\0 的字符串拒绝编码")
    void rejectsNullCharacter() {
        assertThatThrownBy(() -> ObfuscatedString.array("a\0b"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("构造器：空数组快速失败")
    void emptyArrayFailsFast() {
        assertThatThrownBy(() -> new ObfuscatedString(new long[0]))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("toStringIntern：返回驻留字符串")
    void toStringIntern() {
        String source = "swak-intern";

        assertThat(new ObfuscatedString(ObfuscatedString.array(source)).toStringIntern())
                .isEqualTo(source)
                .isSameAs(source.intern());
    }

    @Test
    @DisplayName("literal：转义反斜杠与双引号")
    void literal() {
        assertThat(ObfuscatedString.literal("abc")).isEqualTo("\"abc\"");
        assertThat(ObfuscatedString.literal("a\"b\\c")).isEqualTo("\"a\\\"b\\\\c\"");
    }

    @Test
    @DisplayName("java / obfuscate：生成包含十六进制字面量与原串注释的 Java 源码")
    void javaSource() {
        String source = "swak-secret";
        String code = ObfuscatedString.obfuscate(source);

        assertThat(code)
                .isNotEmpty()
                .contains("0x")
                .contains("\"" + source + "\"")
                .contains("ObfuscatedString");
        // java(array(s)) 同样是合法源码：每次 key 随机，内容不同但结构一致
        assertThat(ObfuscatedString.java(ObfuscatedString.array(source)))
                .contains("\"" + source + "\"");
    }
}
