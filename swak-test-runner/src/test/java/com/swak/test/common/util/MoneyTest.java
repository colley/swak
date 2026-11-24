package com.swak.test.common.util;

import com.swak.common.util.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Money} 的币种、取整与算术回归。
 * <p>
 * 重点关注两类容易出错的地方：
 * <ul>
 *     <li>不同币种的元/分换算比例（人民币 100、日元 1）；</li>
 *     <li>取整模式，以及 allocate 必须保证零头不丢失。</li>
 * </ul>
 */
@DisplayName("Money 货币")
class MoneyTest {

    private static final Currency CNY = Currency.getInstance("CNY");

    private static final Currency USD = Currency.getInstance("USD");

    private static final Currency JPY = Currency.getInstance("JPY");

    @Test
    @DisplayName("构造器：元分、纯分、字符串、double、BigDecimal 都换算成最小单位")
    void constructors() {
        assertThat(new Money().getCent()).isZero();
        assertThat(new Money(10L, 50).getCent()).isEqualTo(1050L);
        assertThat(new Money(1050L).getCent()).isEqualTo(1050L);
        assertThat(new Money("10.50").getCent()).isEqualTo(1050L);
        assertThat(new Money(10.5d).getCent()).isEqualTo(1050L);
        assertThat(new Money(new BigDecimal("10.50")).getCent()).isEqualTo(1050L);

        // 元分里多出来的分按取模处理
        assertThat(new Money(10L, 200).getCent()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("字符串构造：10.5 与 10.50 金额相等")
    void scaleIsNormalized() {
        assertThat(new Money("10.5")).isEqualTo(new Money("10.50"));
        assertThat(new Money("10.5").hashCode()).isEqualTo(new Money("10.50").hashCode());
    }

    @Test
    @DisplayName("取整模式：ROUND_DOWN 直接截断")
    void customRoundingMode() {
        assertThat(new Money(new BigDecimal("10.999"), BigDecimal.ROUND_DOWN).getCent()).isEqualTo(1099L);
        assertThat(new Money("10.999", CNY, BigDecimal.ROUND_UP).getCent()).isEqualTo(1100L);
        // 默认 HALF_EVEN
        assertThat(new Money(new BigDecimal("10.995")).getCent()).isEqualTo(1100L);
    }

    @Test
    @DisplayName("币种换算比例：人民币/美元为 100，日元为 1")
    void centFactorByCurrency() {
        assertThat(new Money("1").getCentFactor()).isEqualTo(100);
        assertThat(new Money("1", USD).getCentFactor()).isEqualTo(100);
        assertThat(new Money("1", JPY).getCentFactor()).isEqualTo(1);
        assertThat(new Money("100", JPY).getCent()).isEqualTo(100L);
        assertThat(new Money("100", JPY).getAmount()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("getAmount：分为单位换算回元")
    void getAmount() {
        assertThat(new Money(1050L).getAmount()).isEqualByComparingTo("10.50");
        assertThat(new Money(0L).getAmount()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("getCurrencyCode / getCurrency / ZERO")
    void currencyAccessors() {
        assertThat(new Money("1").getCurrencyCode()).isEqualTo("CNY");
        assertThat(new Money("1").getCurrency()).isEqualTo(CNY);
        assertThat(Money.DEFAULT_CURRENCY_CODE).isEqualTo("CNY");
        assertThat(Money.ZERO.getCent()).isZero();
        assertThat(Money.ZERO.toString()).isEqualTo("0");
    }

    @Test
    @DisplayName("equals：金额与币种都相同才相等")
    void equalsSemantics() {
        Money tenYuan = new Money("10");

        assertThat(tenYuan).isEqualTo(new Money("10.00"));
        assertThat(tenYuan.equals((Money) null)).isFalse();
        assertThat(tenYuan.equals((Object) "10")).isFalse();
        assertThat(tenYuan.equals((Object) new Money("10", USD))).isFalse();
        assertThat(tenYuan.equals(Money.ZERO)).isFalse();
    }

    @Test
    @DisplayName("compareTo / greaterThan：金额比较，币种不同直接抛异常")
    void compareTo() {
        assertThat(new Money("1").compareTo(new Money("2"))).isEqualTo(-1);
        assertThat(new Money("2").compareTo(new Money("2"))).isZero();
        assertThat(new Money("3").compareTo(new Money("2"))).isEqualTo(1);

        assertThat(new Money("3").greaterThan(new Money("2"))).isTrue();
        assertThat(new Money("1").greaterThan(new Money("2"))).isFalse();

        assertThatThrownBy(() -> new Money("1").compareTo(new Money("1", USD)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency mismatch");
    }

    @Test
    @DisplayName("加减法：add/subtract 不改变原对象，addTo/subtractFrom 原地修改")
    void addAndSubtract() {
        Money ten = new Money("10");
        Money two = new Money("2");

        assertThat(ten.add(two).getCent()).isEqualTo(1200L);
        assertThat(ten.getCent()).isEqualTo(1000L);
        assertThat(ten.subtract(two).getCent()).isEqualTo(800L);

        Money mutable = new Money("10");
        assertThat(mutable.addTo(two)).isSameAs(mutable);
        assertThat(mutable.getCent()).isEqualTo(1200L);
        assertThat(mutable.subtractFrom(two)).isSameAs(mutable);
        assertThat(mutable.getCent()).isEqualTo(1000L);

        assertThatThrownBy(() -> ten.add(new Money("1", USD))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ten.addTo(new Money("1", USD))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ten.subtract(new Money("1", USD))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ten.subtractFrom(new Money("1", USD))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("乘法：long / double / BigDecimal 以及原地版本")
    void multiply() {
        Money ten = new Money("10");

        assertThat(ten.multiply(3L).getCent()).isEqualTo(3000L);
        assertThat(ten.multiply(1.25d).getCent()).isEqualTo(1250L);
        assertThat(ten.multiply(new BigDecimal("0.5")).getCent()).isEqualTo(500L);
        assertThat(ten.multiply(new BigDecimal("1.234"), BigDecimal.ROUND_DOWN).getCent()).isEqualTo(1234L);
        assertThat(ten.getCent()).isEqualTo(1000L);

        Money mutable = new Money("10");
        assertThat(mutable.multiplyBy(2L)).isSameAs(mutable);
        assertThat(mutable.getCent()).isEqualTo(2000L);
        assertThat(mutable.multiplyBy(0.5d).getCent()).isEqualTo(1000L);
        assertThat(mutable.multiplyBy(new BigDecimal("3")).getCent()).isEqualTo(3000L);
        assertThat(mutable.multiplyBy(new BigDecimal("0.5"), BigDecimal.ROUND_DOWN).getCent()).isEqualTo(1500L);
    }

    @Test
    @DisplayName("除法：double / BigDecimal 以及原地版本")
    void divide() {
        Money ten = new Money("10");

        assertThat(ten.divide(2.0d).getCent()).isEqualTo(500L);
        assertThat(ten.divide(new BigDecimal("4")).getCent()).isEqualTo(250L);
        assertThat(ten.divide(new BigDecimal("3"), BigDecimal.ROUND_DOWN).getCent()).isEqualTo(333L);
        assertThat(ten.getCent()).isEqualTo(1000L);

        Money mutable = new Money("10");
        assertThat(mutable.divideBy(2.0d)).isSameAs(mutable);
        assertThat(mutable.getCent()).isEqualTo(500L);
        assertThat(mutable.divideBy(new BigDecimal("5")).getCent()).isEqualTo(100L);
        assertThat(mutable.divideBy(new BigDecimal("3"), BigDecimal.ROUND_DOWN).getCent()).isEqualTo(33L);
    }

    @Test
    @DisplayName("allocate(int)：零头先分给前面的份额，总额不丢失")
    void allocateByTargets() {
        Money[] parts = new Money(100L).allocate(3);

        assertThat(parts).hasSize(3);
        assertThat(parts[0].getCent()).isEqualTo(34L);
        assertThat(parts[1].getCent()).isEqualTo(33L);
        assertThat(parts[2].getCent()).isEqualTo(33L);
        assertThat(parts[0].getCent() + parts[1].getCent() + parts[2].getCent()).isEqualTo(100L);
        assertThat(parts[0].getCurrencyCode()).isEqualTo("CNY");
    }

    @Test
    @DisplayName("allocate(long[])：按比例分配，零头从第一份开始补")
    void allocateByRatios() {
        Money[] parts = new Money(100L).allocate(new long[]{1, 1, 1});

        assertThat(parts).hasSize(3);
        assertThat(parts[0].getCent()).isEqualTo(34L);
        assertThat(parts[1].getCent()).isEqualTo(33L);
        assertThat(parts[2].getCent()).isEqualTo(33L);

        Money[] half = new Money(100L).allocate(new long[]{1, 3});
        assertThat(half[0].getCent()).isEqualTo(25L);
        assertThat(half[1].getCent()).isEqualTo(75L);
    }

    @Test
    @DisplayName("toString / dump：金额去尾零，dump 输出内部字段")
    void toStringAndDump() {
        assertThat(new Money("10.50").toString()).isEqualTo("10.5");
        assertThat(new Money("10").toString()).isEqualTo("10");

        String dump = new Money("10.50").dump();
        assertThat(dump).contains("cent = 1050").contains("currency = CNY");
    }

    @Test
    @DisplayName("setAmount / setCent：可变入口")
    void setters() {
        Money money = new Money();
        money.setAmount(new BigDecimal("12.34"));
        assertThat(money.getCent()).isEqualTo(1234L);

        money.setAmount(null);
        assertThat(money.getCent()).isEqualTo(1234L);

        money.setCent(99L);
        assertThat(money.getCent()).isEqualTo(99L);
    }
}
