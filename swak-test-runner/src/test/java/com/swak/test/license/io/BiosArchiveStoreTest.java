package com.swak.test.license.io;

import com.swak.license.api.io.ArchiveEntrySource;
import com.swak.license.api.io.ArchiveInputStream;
import com.swak.license.api.io.ArchiveStore;
import com.swak.license.api.io.bios.BIOS;
import com.swak.license.api.io.spi.ArchiveEntryNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 归档存储（目录 / ZIP / JAR）与条目名称规范化回归。
 */
@DisplayName("BIOS 归档存储")
class BiosArchiveStoreTest {

    private static final String FIRST = "first-entry";
    private static final String SECOND = "second-entry";

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("目录存储：写入文件与子目录后可遍历、按名查询")
    void directoryStoreRoundTrip() throws Exception {
        Path dir = Files.createDirectories(tempDir.resolve("dir"));
        ArchiveStore store = BIOS.directory(dir);

        store.acceptWriter(out -> {
            out.sink("a.txt").acceptWriter(w -> w.write(FIRST.getBytes(StandardCharsets.UTF_8)));
            out.sink("sub/b.txt").acceptWriter(w -> w.write(SECOND.getBytes(StandardCharsets.UTF_8)));
        });

        Map<String, Boolean> entries = new TreeMap<>();
        try (ArchiveInputStream in = store.input().get()) {
            for (ArchiveEntrySource entry : in) {
                entries.put(entry.name(), entry.directory());
            }
        }
        assertThat(entries)
                .containsEntry("a.txt", false)
                .containsEntry("sub/", true)
                .containsEntry("sub/b.txt", false);

        String content = store.input().apply(in -> readAll(in.source("a.txt").orElseThrow()));
        assertThat(content).isEqualTo(FIRST);

        Optional<ArchiveEntrySource> missing = store.input().apply(in -> in.source("nope.txt"));
        assertThat(missing).isEmpty();
    }

    @Test
    @DisplayName("archive copy：目录之间的条目可完整复制")
    void copiesBetweenDirectoryStores() throws Exception {
        Path src = Files.createDirectories(tempDir.resolve("src"));
        Path dst = tempDir.resolve("dst");
        ArchiveStore source = BIOS.directory(src);
        source.acceptWriter(out -> {
            out.sink("a.txt").acceptWriter(w -> w.write(FIRST.getBytes(StandardCharsets.UTF_8)));
            out.sink("nested/b.txt").acceptWriter(w -> w.write(SECOND.getBytes(StandardCharsets.UTF_8)));
        });

        BIOS.copy(source, BIOS.directory(dst));

        assertThat(Files.readString(dst.resolve("a.txt"))).isEqualTo(FIRST);
        assertThat(Files.readString(dst.resolve("nested/b.txt"))).isEqualTo(SECOND);
    }

    @Test
    @DisplayName("ZIP 存储：写入条目后可读回，缺失条目返回空")
    void zipStoreRoundTrip() throws Exception {
        Path zip = tempDir.resolve("entries.zip");
        ArchiveStore store = BIOS.zip(zip.toFile());

        store.acceptWriter(out -> {
            out.sink("a.txt").acceptWriter(w -> w.write(FIRST.getBytes(StandardCharsets.UTF_8)));
            out.sink("b.txt").acceptWriter(w -> w.write(SECOND.getBytes(StandardCharsets.UTF_8)));
        });

        try (ArchiveInputStream in = store.input().get()) {
            List<String> names = new ArrayList<>();
            for (ArchiveEntrySource entry : in) {
                names.add(entry.name());
            }
            assertThat(names).containsExactly("a.txt", "b.txt");
            assertThat(in.source("missing.txt")).isEmpty();
        }

        String content = store.input().apply(in -> readAll(in.source("b.txt").orElseThrow()));
        assertThat(content).isEqualTo(SECOND);
    }

    @Test
    @DisplayName("JAR 存储：isJar 为真，条目可写入并读回")
    void jarStoreFlagsJar() throws Exception {
        Path jar = tempDir.resolve("entries.jar");
        ArchiveStore store = BIOS.jar(jar.toFile());

        store.acceptWriter(out -> {
            assertThat(out.isJar()).isTrue();
            out.sink("META-INF/readme.txt").acceptWriter(w -> w.write(FIRST.getBytes(StandardCharsets.UTF_8)));
        });

        String content = store.input().apply(in -> readAll(in.source("META-INF/readme.txt").orElseThrow()));
        assertThat(content).isEqualTo(FIRST);
        assertThat(BIOS.jar(jar.toString())).isNotNull();
    }

    @Test
    @DisplayName("条目名称：规范化相对路径，绝对路径、空名与 .. 开头被拒绝")
    void archiveEntryNameNormalization() {
        assertThat(ArchiveEntryNames.requireInternal("a/b.txt")).isEqualTo("a/b.txt");
        assertThat(ArchiveEntryNames.requireInternal("./a/b.txt")).isEqualTo("a/b.txt");
        assertThat(ArchiveEntryNames.requireInternal("a/./b")).isEqualTo("a/b");
        assertThat(ArchiveEntryNames.requireInternal("dir/")).isEqualTo("dir/");

        assertThatThrownBy(() -> ArchiveEntryNames.requireInternal("/absolute"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be absolute");
        assertThatThrownBy(() -> ArchiveEntryNames.requireInternal(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ArchiveEntryNames.requireInternal("../escape"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(ArchiveEntryNames.isInternal("a/b")).isTrue();
        assertThat(ArchiveEntryNames.isInternal("/a")).isFalse();
        assertThat(ArchiveEntryNames.isInternal("../a")).isFalse();
    }

    @Test
    @DisplayName("条目值语义：同名同大小条目相等，toString 带名称与类型")
    void archiveEntryValueSemantics() throws Exception {
        Path zip = tempDir.resolve("semantics.zip");
        ArchiveStore store = BIOS.zip(zip.toFile());
        store.acceptWriter(out -> out.sink("a.txt").acceptWriter(w -> w.write(FIRST.getBytes(StandardCharsets.UTF_8))));

        try (ArchiveInputStream in = store.input().get()) {
            ArchiveEntrySource one = in.source("a.txt").orElseThrow();
            ArchiveEntrySource two = in.source("a.txt").orElseThrow();

            assertThat(one).isEqualTo(two).hasSameHashCodeAs(two);
            assertThat(one).isNotEqualTo(null);
            assertThat(one.toString())
                    .contains("name=a.txt")
                    .contains("directory=false");
        }
    }

    private static String readAll(ArchiveEntrySource source) throws Exception {
        return source.input().apply(in -> new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
}
