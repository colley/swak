package com.swak.test.license.api;

import com.swak.license.api.Codec;
import com.swak.license.api.io.Decoder;
import com.swak.license.api.io.Encoder;
import com.swak.license.api.io.Socket;
import com.swak.license.core.spi.Codecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 按 Content-Transfer-Encoding 与 Content-Type 推导字符集的回归。
 */
@DisplayName("编解码器字符集推导")
class CodecsCharsetTest {

    @Test
    @DisplayName("7bit / quoted-printable / base64 一律返回 US-ASCII")
    void asciiEncodings() {
        assertThat(Codecs.charset(codec("7bit", "text/plain")))
                .isEqualTo(Optional.of(StandardCharsets.US_ASCII));
        assertThat(Codecs.charset(codec("QUOTED-PRINTABLE", "text/plain")))
                .isEqualTo(Optional.of(StandardCharsets.US_ASCII));
        assertThat(Codecs.charset(codec("base64", "application/octet-stream")))
                .isEqualTo(Optional.of(StandardCharsets.US_ASCII));
    }

    @Test
    @DisplayName("8bit 且带 charset 参数时按参数解析（引号与裸值都支持）")
    void eightBitUsesDeclaredCharset() {
        assertThat(Codecs.charset(codec("8bit", "application/json; charset=\"UTF-8\"")))
                .isEqualTo(Optional.of(StandardCharsets.UTF_8));
        assertThat(Codecs.charset(codec("8BIT", "text/plain; charset=ISO-8859-1")))
                .isEqualTo(Optional.of(StandardCharsets.ISO_8859_1));
    }

    @Test
    @DisplayName("8bit 未声明 charset 时回退 UTF-8")
    void eightBitDefaultsToUtf8() {
        assertThat(Codecs.charset(codec("8bit", "application/json")))
                .isEqualTo(Optional.of(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("其它编码返回空，表示不产生文本")
    void otherEncodingsReturnEmpty() {
        assertThat(Codecs.charset(codec("binary", "application/octet-stream"))).isEmpty();
        assertThat(Codecs.charset(codec(null, "text/plain"))).isEmpty();
    }

    @Test
    @DisplayName("无法识别的 charset 名称被拒绝")
    void unknownCharsetRejected() {
        assertThatThrownBy(() -> Codecs.charset(codec("8bit", "text/plain; charset=\"NoSuchCharset\"")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Codec codec(String transferEncoding, String contentType) {
        return new Codec() {

            @Override
            public String contentType() {
                return contentType;
            }

            @Override
            public String contentTransferEncoding() {
                return transferEncoding;
            }

            @Override
            public Encoder encoder(Socket<OutputStream> output) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Decoder decoder(Socket<InputStream> input) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
