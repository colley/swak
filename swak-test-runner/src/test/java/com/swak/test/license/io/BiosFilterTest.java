package com.swak.test.license.io;

import com.swak.license.api.io.Codec;
import com.swak.license.api.io.ContentTooLargeException;
import com.swak.license.api.io.Filter;
import com.swak.license.api.io.Store;
import com.swak.license.api.io.bios.BIOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code BIOS} 过滤器语义回归：所有过滤器都应在 {@code Store} 上形成可逆往返，
 * 并且 {@code compose} 的组合顺序要与「输出先本过滤器、输入先另一过滤器」一致。
 */
@DisplayName("BIOS 过滤器")
class BiosFilterTest {

    private static final String TEXT = "swak-license-" + "abcdefghij".repeat(20);
    private static final byte[] TEXT_BYTES = TEXT.getBytes(StandardCharsets.UTF_8);

    @Test
    @DisplayName("Base64 过滤器：底层存编码文本，读取还原原文")
    void base64Filter() throws Exception {
        Store base = BIOS.memory();
        Store filtered = base.map(BIOS.base64());

        filtered.content(TEXT_BYTES);

        assertThat(base.content()).isEqualTo(Base64.getEncoder().encode(TEXT_BYTES));
        assertThat(filtered.content()).isEqualTo(TEXT_BYTES);
    }

    @Test
    @DisplayName("缓冲过滤器：往返一致，非正缓冲区大小被拒绝")
    void bufferFilter() throws Exception {
        Store store = BIOS.memory().map(BIOS.buffer(16));
        store.content(TEXT_BYTES);

        assertThat(store.content()).isEqualTo(TEXT_BYTES);
        assertThat(BIOS.buffer()).isNotNull();
        assertThatThrownBy(() -> BIOS.buffer(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("<= 0");
    }

    @Test
    @DisplayName("GZIP 过滤器：往返一致且底层内容被压缩")
    void gzipFilter() throws Exception {
        Store base = BIOS.memory();
        Store filtered = base.map(BIOS.gzip());

        filtered.content(TEXT_BYTES);

        assertThat(filtered.content()).isEqualTo(TEXT_BYTES);
        assertThat(base.content()).isNotEqualTo(TEXT_BYTES);
        assertThat(base.content().length).isLessThan(TEXT_BYTES.length);
    }

    @Test
    @DisplayName("Deflate 过滤器：往返一致，压缩级别越界被拒绝")
    void deflateFilter() throws Exception {
        Store store = BIOS.memory().map(BIOS.deflate(Deflater.BEST_COMPRESSION));
        store.content(TEXT_BYTES);
        assertThat(store.content()).isEqualTo(TEXT_BYTES);

        assertThat(BIOS.deflate()).isNotNull();
        assertThat(BIOS.deflate(() -> new Deflater(Deflater.BEST_SPEED), Inflater::new)).isNotNull();

        assertThatThrownBy(() -> BIOS.deflate(Deflater.BEST_COMPRESSION + 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in the range");
        assertThatThrownBy(() -> BIOS.deflate(Deflater.DEFAULT_COMPRESSION - 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Inflate 过滤器：写入压缩数据得到原文，读出的数据仍是压缩流")
    void inflateFilterInvertsDeflate() throws Exception {
        Store base = BIOS.memory();
        Store inflating = base.map(BIOS.inflate());

        inflating.content(deflate(TEXT_BYTES));
        assertThat(base.content()).isEqualTo(TEXT_BYTES);

        assertThat(inflate(inflating.content())).isEqualTo(TEXT_BYTES);

        assertThat(BIOS.inflate()).isNotNull();
        assertThat(BIOS.inflate(Inflater::new, () -> new Deflater(Deflater.BEST_SPEED))).isNotNull();
        assertThatThrownBy(() -> BIOS.inflate(Deflater.BEST_COMPRESSION + 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BIOS.inflate(Deflater.DEFAULT_COMPRESSION - 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cipher 过滤器：AES 加密后往返一致，底层内容被加密")
    void cipherFilter() throws Exception {
        SecretKeySpec key = new SecretKeySpec("0123456789abcdef".getBytes(StandardCharsets.UTF_8), "AES");
        Filter aes = BIOS.cipher(encrypt -> {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key);
            return cipher;
        });

        Store base = BIOS.memory();
        Store filtered = base.map(aes);

        filtered.content(TEXT_BYTES);

        assertThat(filtered.content()).isEqualTo(TEXT_BYTES);
        assertThat(base.content()).isNotEqualTo(TEXT_BYTES);
    }

    @Test
    @DisplayName("identity 过滤器不改变存储，compose 单位元直接返回另一端")
    void identityFilter() {
        Store store = BIOS.memory();
        Filter gzip = BIOS.gzip();

        assertThat(store.map(BIOS.identity())).isSameAs(store);
        assertThat(BIOS.identity().compose(gzip)).isSameAs(gzip);
    }

    @Test
    @DisplayName("compose 组合：输出按先本过滤器、输入按先后一过滤器的顺序执行")
    void composedFilterRoundTrip() throws Exception {
        // 输出：先 GZIP 再 Base64；输入：先 Base64 解码再 GZIP 解压。
        Filter composed = BIOS.gzip().compose(BIOS.base64());

        Store base = BIOS.memory();
        Store filtered = base.map(composed);
        filtered.content(TEXT_BYTES);

        assertThat(filtered.content()).isEqualTo(TEXT_BYTES);
        // 底层内容应为 GZIP 之后再 Base64 的文本：先解 Base64 再 gunzip 即可还原。
        assertThat(gunzip(Base64.getDecoder().decode(base.content()))).isEqualTo(TEXT_BYTES);
    }

    @Test
    @DisplayName("andThen 与 compose 顺序相反，往返仍然一致")
    @SuppressWarnings("deprecation")
    void andThenIsInverseOfCompose() throws Exception {
        Filter reversed = BIOS.base64().andThen(BIOS.gzip());
        Store store = BIOS.memory().map(reversed);
        store.content(TEXT_BYTES);

        assertThat(store.content()).isEqualTo(TEXT_BYTES);
    }

    @Test
    @DisplayName("过滤后的 Store：大小与删除委托给底层，内容读取仍然超限校验")
    void filteredStoreDelegates() throws Exception {
        Store base = BIOS.memory();
        Store filtered = base.map(BIOS.gzip());
        filtered.content(TEXT_BYTES);

        assertThat(filtered.size()).isEqualTo(base.size());

        assertThatThrownBy(() -> filtered.content(3)).isInstanceOf(ContentTooLargeException.class);
        assertThatThrownBy(() -> filtered.content(-1)).isInstanceOf(IllegalArgumentException.class);

        filtered.delete();
        assertThat(base.exists()).isFalse();
    }

    @Test
    @DisplayName("过滤器可作用于 Codec：编解码往返一致")
    void filterAppliesToCodec() throws Exception {
        Store store = BIOS.memory();
        Codec compressed = BIOS.serialization().map(BIOS.gzip());

        compressed.connect(store).encode(TEXT);
        String decoded = compressed.connect(store).decode(String.class);
        assertThat(decoded).isEqualTo(TEXT);
    }

    private static byte[] deflate(byte[] data) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DeflaterOutputStream out = new DeflaterOutputStream(bytes)) {
            out.write(data);
        }
        return bytes.toByteArray();
    }

    private static byte[] inflate(byte[] data) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InflaterOutputStream out = new InflaterOutputStream(bytes)) {
            out.write(data);
        }
        return bytes.toByteArray();
    }

    private static byte[] gunzip(byte[] data) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data))) {
            in.transferTo(bytes);
        }
        return bytes.toByteArray();
    }
}
