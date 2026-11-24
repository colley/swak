package com.swak.test.license.io;

import com.swak.license.api.io.ContentTooLargeException;
import com.swak.license.api.io.NoContentException;
import com.swak.license.api.io.Sink;
import com.swak.license.api.io.Source;
import com.swak.license.api.io.Store;
import com.swak.license.api.io.bios.BIOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalLong;
import java.util.prefs.Preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code BIOS} 存储层的语义回归：内存 / 文件 / 首选项三种实现共用
 * {@link Store} 的 exists / size / content / delete 契约。
 */
@DisplayName("BIOS 存储层")
class BiosStoreTest {

    private static final byte[] PAYLOAD = "swak-license".getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("内存存储：未写入时不存在，读内容抛 NoContentException")
    void memoryStoreIsEmptyInitially() throws Exception {
        Store store = BIOS.memory();

        assertThat(store.exists()).isFalse();
        assertThat(store.size()).isEqualTo(OptionalLong.empty());
        assertThatThrownBy(store::content).isInstanceOf(NoContentException.class);
        assertThatThrownBy(() -> store.input().get()).isInstanceOf(NoContentException.class);
    }

    @Test
    @DisplayName("内存存储：整体写入后可读回，再次打开输出流是覆盖而非追加")
    void memoryStoreRoundTrip() throws Exception {
        Store store = BIOS.memory(16);

        store.content(PAYLOAD);
        assertThat(store.exists()).isTrue();
        assertThat(store.size()).isEqualTo(OptionalLong.of(PAYLOAD.length));
        assertThat(store.content()).isEqualTo(PAYLOAD);

        store.acceptWriter(out -> out.write("overwrite".getBytes(StandardCharsets.UTF_8)));
        assertThat(new String(store.content(), StandardCharsets.UTF_8)).isEqualTo("overwrite");
        assertThat(store.size()).isEqualTo(OptionalLong.of("overwrite".length()));
    }

    @Test
    @DisplayName("内存存储：区间写入只保留指定片段")
    void memoryStoreWritesSlice() throws Exception {
        Store store = BIOS.memory();

        store.content(PAYLOAD, 5, 7);

        assertThat(new String(store.content(), StandardCharsets.UTF_8)).isEqualTo("license");
    }

    @Test
    @DisplayName("内存存储：负缓冲区大小被拒绝")
    void memoryRejectsNegativeBuffer() {
        assertThatThrownBy(() -> BIOS.memory(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative buffer size");
    }

    @Test
    @DisplayName("内容读取：超过上限抛 ContentTooLargeException，负数上限抛 IllegalArgumentException")
    void contentLimits() throws Exception {
        Store store = BIOS.memory();
        store.content(PAYLOAD);

        assertThatThrownBy(() -> store.content(3)).isInstanceOf(ContentTooLargeException.class);
        assertThatThrownBy(() -> store.content(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("删除：delete 后回到不存在状态，deleteIfExists 幂等")
    void deleteSemantics() throws Exception {
        Store store = BIOS.memory();
        store.content(PAYLOAD);

        store.delete();
        assertThat(store.exists()).isFalse();

        store.deleteIfExists();
        assertThat(store.exists()).isFalse();
    }

    @Test
    @DisplayName("文件存储：写入、读取、大小、追加与删除")
    void fileStoreRoundTrip() throws Exception {
        Path path = tempDir.resolve("store.bin");
        Store store = BIOS.file(path);

        assertThat(store.exists()).isFalse();
        assertThat(store.size()).isEqualTo(OptionalLong.empty());

        store.content(PAYLOAD);
        assertThat(store.content()).isEqualTo(PAYLOAD);
        assertThat(store.size()).isEqualTo(OptionalLong.of(PAYLOAD.length));

        Store appending = BIOS.file(path, true);
        appending.acceptWriter(out -> out.write("!".getBytes(StandardCharsets.UTF_8)));
        assertThat(store.content()).hasSize(PAYLOAD.length + 1);

        store.delete();
        assertThat(Files.exists(path)).isFalse();
        store.deleteIfExists();
    }

    @Test
    @DisplayName("文件存储：File / String / Path 重载等价，onInput 与 onOutput 派生新实例")
    void fileStoreFactories() throws Exception {
        File file = tempDir.resolve("factory.bin").toFile();

        BIOS.FileStore byFile = BIOS.file(file);
        BIOS.FileStore byString = BIOS.file(file.getAbsolutePath());
        byFile.content("a".getBytes(StandardCharsets.UTF_8));
        assertThat(byString.content()).isEqualTo("a".getBytes(StandardCharsets.UTF_8));

        BIOS.PathStore pathStore = BIOS.path(file.toPath());
        assertThat(pathStore.onInput()).isNotSameAs(pathStore);
        assertThat(pathStore.onOutput()).isNotSameAs(pathStore);
        assertThat(BIOS.path(file.toPath(), false)).isNotNull();
        assertThat(BIOS.file(file, false)).isNotNull();
    }

    @Test
    @DisplayName("首选项存储：写入后可读、删除后回到无内容")
    void preferencesStoreRoundTrip() throws Exception {
        Preferences node = Preferences.userRoot().node("com/swak/test/license/io/BiosStoreTest");
        try {
            node.remove("blob");
            Store store = BIOS.preferences(node, "blob");

            assertThat(store.exists()).isFalse();
            assertThatThrownBy(store::content)
                    .isInstanceOf(NoContentException.class)
                    .hasMessageContaining("blob");

            store.content(PAYLOAD);
            assertThat(store.content()).isEqualTo(PAYLOAD);
            assertThat(store.size()).isEqualTo(OptionalLong.of(PAYLOAD.length));

            store.delete();
            assertThat(store.exists()).isFalse();
        } finally {
            node.removeNode();
        }
    }

    @Test
    @DisplayName("流包装：关闭只 flush 不关闭底层流，可重复写入")
    void uncloseableStreams() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Sink sink = BIOS.stream(bytes);

        sink.acceptWriter(out -> out.write("swak".getBytes(StandardCharsets.UTF_8)));
        sink.acceptWriter(out -> out.write("-license".getBytes(StandardCharsets.UTF_8)));
        assertThat(bytes.toString(StandardCharsets.UTF_8)).isEqualTo("swak-license");

        Source source = BIOS.stream(new ByteArrayInputStream(PAYLOAD));
        assertThat(BIOS.content(source)).isEqualTo(PAYLOAD);
    }

    @Test
    @DisplayName("内容读取：Source 超限抛 ContentTooLargeException")
    void contentOfSourceRespectsLimit() throws Exception {
        Source source = BIOS.stream(new ByteArrayInputStream(PAYLOAD));

        assertThatThrownBy(() -> BIOS.content(source, PAYLOAD.length - 1))
                .isInstanceOf(ContentTooLargeException.class)
                .hasMessageContaining("exceeds");
    }

    @Test
    @DisplayName("资源与 URL 来源：可读取内容，缺失资源抛 FileNotFoundException")
    void resourceAndUrlSources() throws Exception {
        String className = BiosStoreTest.class.getName().replace('.', '/') + ".class";

        byte[] classBytes = BIOS.content(BIOS.resource(className));
        assertThat(classBytes).startsWith((byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE);
        assertThat(BIOS.content(BIOS.resource(className, null))).isEqualTo(classBytes);

        assertThatThrownBy(() -> BIOS.content(BIOS.resource("no/such/resource.txt")))
                .isInstanceOf(FileNotFoundException.class);

        Path file = tempDir.resolve("url.txt");
        Files.write(file, PAYLOAD);
        assertThat(BIOS.content(BIOS.url(file.toUri().toURL()))).isEqualTo(PAYLOAD);
    }

    @Test
    @DisplayName("标准流工厂返回可用实例")
    void standardStreams() {
        assertThat(BIOS.stdin()).isNotNull();
        assertThat(BIOS.stdout()).isNotNull();
        assertThat(BIOS.stderr()).isNotNull();
    }
}
