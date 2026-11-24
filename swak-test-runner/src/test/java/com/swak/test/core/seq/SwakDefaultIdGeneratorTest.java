package com.swak.test.core.seq;

import com.swak.core.seq.SwakDefaultIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SwakDefaultIdGenerator} 的压缩 ID 生成。
 * <p>
 * 两个构造器都不是 public，机器号由 MAC/PID 哈希推导，因此这里统一走反射构造，
 * 并通过反射改写内部状态来覆盖「时钟回拨」「同一 tick 序号用尽」这类正常路径难以触发的分支。
 */
@DisplayName("压缩型默认 ID 生成器")
class SwakDefaultIdGeneratorTest {

    private static final long MAX_53_BITS = 9007199254740991L;

    @Test
    @DisplayName("默认构造：ID 落在 53 位内、正整数且单调不重复")
    void generatesCompressedIds() throws Exception {
        SwakDefaultIdGenerator generator = newGenerator();

        long previous = -1L;
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Long id = generator.nextId();
            assertThat(id).isNotNull().isPositive().isLessThanOrEqualTo(MAX_53_BITS);
            assertThat(id).isGreaterThan(previous);
            assertThat(seen.add(id)).isTrue();
            previous = id;
        }
    }

    @Test
    @DisplayName("机器号越界（大于 31 或小于 0）：私有构造抛 IllegalArgumentException")
    void rejectsInvalidMachineId() throws Exception {
        Constructor<SwakDefaultIdGenerator> ctor =
                SwakDefaultIdGenerator.class.getDeclaredConstructor(long.class);
        ctor.setAccessible(true);

        assertThatThrownBy(() -> ctor.newInstance(32L))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ctor.newInstance(-1L))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("时钟回拨：lastStamp 超前时拒绝生成")
    void rejectsClockBackwards() throws Exception {
        SwakDefaultIdGenerator generator = newGenerator();
        setLong(generator, "lastStamp", System.currentTimeMillis() / 10 + 100_000L);

        assertThatThrownBy(generator::nextId)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Clock moved backwards");
    }

    @Test
    @DisplayName("同一 10ms 内 256 个序号用尽后跳到下一个 tick 继续生成")
    void sequenceOverflowMovesToNextTick() throws Exception {
        SwakDefaultIdGenerator generator = newGenerator();
        // 构造出「当前 tick 已用完序号」的状态；即使调用时恰好跨了 tick，本用例也只断言可继续生成
        setLong(generator, "lastStamp", System.currentTimeMillis() / 10);
        setLong(generator, "sequence", 255L);

        assertThat(generator.nextId()).isNotNull();
        assertThat(generator.nextId()).isNotNull();
    }

    private static SwakDefaultIdGenerator newGenerator() throws Exception {
        Constructor<SwakDefaultIdGenerator> ctor =
                SwakDefaultIdGenerator.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }

    private static void setLong(Object target, String fieldName, long value) throws Exception {
        Field field = SwakDefaultIdGenerator.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setLong(target, value);
    }
}
