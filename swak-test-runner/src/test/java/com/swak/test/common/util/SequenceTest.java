package com.swak.test.common.util;

import com.swak.common.exception.ArgumentException;
import com.swak.common.util.Sequence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Sequence}（雪花算法）的位布局与参数校验回归。
 * <p>
 * 这里不校验时间戳本身（依赖系统时钟），而是把 workerId / datacenterId
 * 固定后从生成的 ID 里把对应位段抠出来比对，结果与机器、时区无关。
 * <p>
 * 注意：{@code Sequence(long workerId, long datacenterId)} 的入参校验写反了
 * （{@code SwakAssert.state} 的参数是「合法条件」，源码却传了「非法条件」），
 * 导致合法值反而抛异常。本测试锁定的是当前可观测行为，待生产代码修复后需同步调整。
 */
@DisplayName("Sequence 雪花 ID")
class SequenceTest {

    @Test
    @DisplayName("ID 位布局：低 12 位序列、中间 5+5 位机器与机房标识")
    void bitLayout() {
        Sequence sequence = new Sequence();

        long id = sequence.nextId(7L, 21L);

        assertThat(id).isPositive();
        assertThat((id >> 12) & 0x1FL).isEqualTo(7L);
        assertThat((id >> 17) & 0x1FL).isEqualTo(21L);
        assertThat(id & 0xFFFL).isBetween(1L, 3L);
    }

    @Test
    @DisplayName("连续生成的 ID 单调递增且不重复")
    void idsAreUniqueAndIncreasing() {
        Sequence sequence = new Sequence();

        long first = sequence.nextId(1L, 1L);
        long second = sequence.nextId(1L, 1L);
        long third = sequence.nextId(1L, 1L);

        assertThat(second).isGreaterThan(first);
        assertThat(third).isGreaterThan(second);
        assertThat(third).isNotEqualTo(first);
    }

    @Test
    @DisplayName("两参构造器的入参校验（当前实现判断取反）")
    void constructorValidation() {
        assertThatThrownBy(() -> new Sequence(7L, 21L))
                .isInstanceOf(ArgumentException.class)
                .hasMessageContaining("worker Id");
        assertThatThrownBy(() -> new Sequence(0L, 0L))
                .isInstanceOf(ArgumentException.class)
                .hasMessageContaining("worker Id");
        assertThatThrownBy(() -> new Sequence(1L, 32L))
                .isInstanceOf(ArgumentException.class)
                .hasMessageContaining("worker Id");
        assertThatThrownBy(() -> new Sequence(32L, 1L))
                .isInstanceOf(ArgumentException.class)
                .hasMessageContaining("datacenter Id");

        // 两个值都越界时两个断言都不触发，构造反而成功
        assertThat(new Sequence(32L, 32L)).isNotNull();
    }

    @Test
    @DisplayName("默认构造与 factor 构造都能生成 ID")
    void constructorsProduceIds() {
        Sequence defaultSequence = new Sequence();
        Sequence factorSequence = new Sequence("swak-node");

        assertThat(defaultSequence.nextId()).isPositive();
        assertThat(factorSequence.nextId()).isPositive();
        assertThat(factorSequence.nextId("swak-node")).isPositive();
    }

    @Test
    @DisplayName("getInstance 返回同一单例")
    void singleton() {
        assertThat(Sequence.getInstance()).isSameAs(Sequence.getInstance());
        assertThat(Sequence.getInstance().nextId()).isPositive();
    }
}
