package com.swak.test.core;

import com.swak.core.SwakVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SwakVersion} 的版本号解析与比较。
 * <p>
 * 静态字段 VERSION 来自 MANIFEST / Package，测试环境下通常是默认值，
 * 所以对「当前版本」只做格式断言，具体数值用显式构造的版本验证。
 */
@DisplayName("Swak 版本号")
class SwakVersionTest {

    @Test
    @DisplayName("CURRENT / toString / getVersion：格式为 major.minor.micro")
    void currentVersion() {
        assertThat(SwakVersion.CURRENT.toString()).isEqualTo("2.4.0");
        assertThat(SwakVersion.getVersion()).isNotNull().matches("\\d+\\.\\d+\\.\\d+");
    }

    @Test
    @DisplayName("三个参数的构造器：数字与字符串等价")
    void constructors() {
        assertThat(new SwakVersion(1, 2, 3).toString()).isEqualTo("1.2.3");
        assertThat(new SwakVersion("1", "2", "3")).isEqualTo(new SwakVersion(1, 2, 3));
    }

    @Test
    @DisplayName("toVersion / versionStrToInt：整数与点分字符串互转")
    void intAndStringConversion() {
        assertThat(SwakVersion.toVersion(20400)).isEqualTo("2.4.0");
        assertThat(SwakVersion.toVersion(10000)).isEqualTo("1.0.0");
        assertThat(SwakVersion.toVersion(0)).isEqualTo("0.0.0");

        assertThat(SwakVersion.versionStrToInt("2.4.0")).isEqualTo(20400);
        assertThat(SwakVersion.versionStrToInt("1.0.0")).isEqualTo(10000);
        assertThat(SwakVersion.versionStrToInt("1")).isEqualTo(1);
    }

    @Test
    @DisplayName("getVersion(Class, default)：解析 default 中的数字版本，非法输入回退 CURRENT")
    void versionFromClassAndDefault() {
        // 用测试类自己作为 cls：它所在目录没有 MANIFEST，保证走 default 分支。
        // 若传 SwakVersion.class，合跑 verify 时会先读到新构建 jar 的 Implementation-Version，
        // default 被短路，断言就随构建产物漂移。
        assertThat(SwakVersion.getVersion(SwakVersionTest.class, "1.2.3"))
                .isEqualTo(new SwakVersion(1, 2, 3));
        assertThat(SwakVersion.getVersion(SwakVersionTest.class, "3.0.0-SNAPSHOT"))
                .isEqualTo(new SwakVersion(3, 0, 0));
        assertThat(SwakVersion.getVersion(SwakVersionTest.class, "not-a-version"))
                .isEqualTo(SwakVersion.CURRENT);

        // 读得到 manifest 的类：返回值只能是解析结果或回退值，不该抛异常
        assertThat(SwakVersion.getVersion(SwakVersion.class, "")).isNotNull();
    }

    @Test
    @DisplayName("已知缺陷：解析正则的每一位只匹配单个数字，10.x 会被截断成 x")
    void multiDigitVersionIsTruncated() {
        // 正则 "([0-9]).([0-9]).([0-9])" 未转义点号且位数固定，这是当前可观测行为
        assertThat(SwakVersion.getVersion(SwakVersionTest.class, "12.4.0"))
                .isEqualTo(new SwakVersion(2, 4, 0));
    }

    @Test
    @DisplayName("compareTo / isXxx：null 视为最小，相等与大小比较都正确")
    void comparisons() {
        SwakVersion current = new SwakVersion(2, 4, 0);

        assertThat(current.isGreaterThan(new SwakVersion(2, 3, 9))).isTrue();
        assertThat(current.isGreaterThan(new SwakVersion(2, 4, 0))).isFalse();
        assertThat(current.isGreaterThan(null)).isTrue();
        assertThat(current.compareTo(null)).isEqualTo(1);

        assertThat(current.isGreaterThanOrEqualTo(current)).isTrue();
        assertThat(current.isLessThan(new SwakVersion(3, 0, 0))).isTrue();
        assertThat(current.isLessThanOrEqualTo(current)).isTrue();
        assertThat(current.is(new SwakVersion(2, 4, 0))).isTrue();
        assertThat(current.is(new SwakVersion(2, 4, 1))).isFalse();

        // 逐级比较：先 major，再 minor，最后 micro
        assertThat(new SwakVersion(2, 0, 0).compareTo(new SwakVersion(1, 9, 9))).isPositive();
        assertThat(new SwakVersion(2, 4, 0).compareTo(new SwakVersion(2, 3, 9))).isPositive();
        assertThat(new SwakVersion(2, 4, 1).compareTo(new SwakVersion(2, 4, 0))).isPositive();
        assertThat(current.compareTo(current)).isZero();
    }

    @Test
    @DisplayName("equals / hashCode：只看三段数字，与类型无关的入参返回 false")
    void equality() {
        SwakVersion a = new SwakVersion(2, 4, 0);
        SwakVersion b = new SwakVersion(2, 4, 0);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.equals(a)).isTrue();
        assertThat(a).isNotEqualTo(new SwakVersion(2, 4, 1));
        assertThat(a.equals("2.4.0")).isFalse();
        assertThat(a.equals(null)).isFalse();
    }
}
