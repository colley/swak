package com.swak.test.common.util.stream;

import com.swak.common.util.stream.FastByteArrayOutputStream;
import com.swak.common.util.stream.UpdateMessageDigestInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FastByteArrayOutputStream} 的分块缓冲、读取与摘要回归。
 * <p>
 * 这个类的坑集中在「跨多个内部 block」的读写：block 大小会翻倍，
 * 单字节写入、批量写入、skip、available 都必须与总长度保持一致。
 */
@DisplayName("FastByteArrayOutputStream 快速字节流")
class FastByteArrayOutputStreamTest {

    @Test
    @DisplayName("构造器：block 大小必须大于 0")
    void constructorValidation() {
        assertThatThrownBy(() -> new FastByteArrayOutputStream(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new FastByteArrayOutputStream(1).size()).isZero();
    }

    @Test
    @DisplayName("write(int)：逐字节写入并跨 block")
    void writeSingleByte() throws IOException {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(4);
        for (int i = 0; i < 10; i++) {
            out.write('a' + i);
        }

        assertThat(out.size()).isEqualTo(10);
        assertThat(out.toString()).isEqualTo("abcdefghij");
        assertThat(out.toByteArray()).isEqualTo("abcdefghij".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    @DisplayName("write(byte[],off,len)：一次写入超过 block 大小")
    void writeArrayAcrossBlocks() throws IOException {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(8);
        byte[] data = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);

        out.write(data, 0, data.length);

        assertThat(out.size()).isEqualTo(data.length);
        assertThat(out.toByteArray()).isEqualTo(data);
    }

    @Test
    @DisplayName("write(byte[],off,len)：非法区间抛 IndexOutOfBoundsException")
    void writeArrayValidation() {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        byte[] data = new byte[4];

        assertThatThrownBy(() -> out.write(data, -1, 1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> out.write(data, 0, 5)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> out.write(data, 0, -1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("close 之后写入抛 IOException，reset 可重新写")
    void closeAndReset() throws IOException {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write('x');
        out.close();

        assertThatThrownBy(() -> out.write('y')).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> out.write(new byte[]{'y'}, 0, 1)).isInstanceOf(IOException.class);

        out.reset();
        assertThat(out.size()).isZero();
        out.write('z');
        assertThat(out.toString()).isEqualTo("z");
    }

    @Test
    @DisplayName("toByteArrayUnsafe：连续调用是 no-op；toByteArray 返回副本")
    void toByteArray() throws IOException {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(4);
        out.write("abcd".getBytes(StandardCharsets.US_ASCII));

        byte[] first = out.toByteArrayUnsafe();
        byte[] second = out.toByteArrayUnsafe();
        assertThat(second).isSameAs(first);

        byte[] copy = out.toByteArray();
        assertThat(copy).isEqualTo(first).isNotSameAs(first);
    }

    @Test
    @DisplayName("空流：toByteArray 为空数组，toString 为空串")
    void emptyStream() {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();

        assertThat(out.size()).isZero();
        assertThat(out.toByteArray()).isEmpty();
        assertThat(out.toString()).isEmpty();
    }

    @Test
    @DisplayName("writeTo：跨 block 输出到另一个流")
    void writeTo() throws IOException {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(4);
        byte[] data = "0123456789".getBytes(StandardCharsets.US_ASCII);
        out.write(data, 0, data.length);

        ByteArrayOutputStream target = new ByteArrayOutputStream();
        out.writeTo(target);

        assertThat(target.toByteArray()).isEqualTo(data);
    }

    @Test
    @DisplayName("resize：扩容、缩到当前大小、小于当前大小时报错")
    void resize() throws IOException {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(4);
        byte[] data = "0123456789".getBytes(StandardCharsets.US_ASCII);
        out.write(data, 0, data.length);

        out.resize(20);
        assertThat(out.size()).isEqualTo(10);
        assertThat(out.toByteArray()).isEqualTo(data);

        // 正好等于当前大小且首个 block 长度已匹配时什么都不做
        out.resize(10);
        assertThat(out.size()).isEqualTo(10);

        assertThatThrownBy(() -> out.resize(5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getInputStream：read / available / 无数据时返回 -1")
    void inputStreamRead() throws IOException {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(4);
        byte[] data = "0123456789".getBytes(StandardCharsets.US_ASCII);
        out.write(data, 0, data.length);

        InputStream in = out.getInputStream();
        assertThat(in.available()).isEqualTo(10);
        assertThat(in.read()).isEqualTo('0');

        byte[] buffer = new byte[4];
        assertThat(in.read(buffer)).isEqualTo(4);
        assertThat(new String(buffer, StandardCharsets.US_ASCII)).isEqualTo("1234");

        assertThat(in.read(buffer, 0, 0)).isZero();
        assertThatThrownBy(() -> in.read(buffer, 0, 5)).isInstanceOf(IndexOutOfBoundsException.class);

        // 读完剩下的
        ByteArrayOutputStream rest = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            rest.write(b);
        }
        assertThat(rest.toString(StandardCharsets.US_ASCII)).isEqualTo("56789");
        assertThat(in.read()).isEqualTo(-1);
        assertThat(in.available()).isZero();

        InputStream empty = new FastByteArrayOutputStream().getInputStream();
        assertThat(empty.read()).isEqualTo(-1);
        assertThat(empty.read(new byte[2])).isEqualTo(-1);
    }

    @Test
    @DisplayName("InputStream.skip：跳过跨 block 的字节，非法参数抛异常")
    void inputStreamSkip() throws IOException {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(4);
        out.write("0123456789".getBytes(StandardCharsets.US_ASCII));

        InputStream in = out.getInputStream();
        assertThat(in.skip(0)).isZero();
        assertThat(in.skip(6)).isEqualTo(6);
        assertThat(in.read()).isEqualTo('6');
        assertThat(in.skip(100)).isEqualTo(3);
        assertThat(in.read()).isEqualTo(-1);

        assertThatThrownBy(() -> in.skip(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> in.skip((long) Integer.MAX_VALUE + 1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new FastByteArrayOutputStream().getInputStream().skip(1)).isZero();
    }

    @Test
    @DisplayName("updateMessageDigest：整段与指定长度的摘要一致")
    void messageDigest() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(4);
        byte[] data = "0123456789".getBytes(StandardCharsets.US_ASCII);
        out.write(data, 0, data.length);

        UpdateMessageDigestInputStream in = (UpdateMessageDigestInputStream) out.getInputStream();
        MessageDigest full = MessageDigest.getInstance("MD5");
        in.updateMessageDigest(full);

        MessageDigest expected = MessageDigest.getInstance("MD5");
        expected.update(data);
        assertThat(full.digest()).isEqualTo(expected.digest());

        UpdateMessageDigestInputStream partial = (UpdateMessageDigestInputStream) out.getInputStream();
        MessageDigest firstFive = MessageDigest.getInstance("MD5");
        partial.updateMessageDigest(firstFive, 5);

        MessageDigest expectedFive = MessageDigest.getInstance("MD5");
        expectedFive.update("01234".getBytes(StandardCharsets.US_ASCII));
        assertThat(firstFive.digest()).isEqualTo(expectedFive.digest());
    }

    @Test
    @DisplayName("updateMessageDigest：len 为 0 或负数")
    void messageDigestLengthValidation() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write("abc".getBytes(StandardCharsets.US_ASCII));

        UpdateMessageDigestInputStream in = (UpdateMessageDigestInputStream) out.getInputStream();
        MessageDigest digest = MessageDigest.getInstance("MD5");
        in.updateMessageDigest(digest, 0);
        assertThat(digest.digest()).isEqualTo(MessageDigest.getInstance("MD5").digest());

        UpdateMessageDigestInputStream negative = (UpdateMessageDigestInputStream) out.getInputStream();
        assertThatThrownBy(() -> negative.updateMessageDigest(MessageDigest.getInstance("MD5"), -1))
                .isInstanceOf(IllegalArgumentException.class);

        UpdateMessageDigestInputStream empty = (UpdateMessageDigestInputStream)
                new FastByteArrayOutputStream().getInputStream();
        empty.updateMessageDigest(MessageDigest.getInstance("MD5"), 3);
    }
}
