package com.swak.test.license.core;

import com.swak.license.provider.V4License;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.security.auth.x500.X500Principal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 许可证值语义回归：{@code LicenseStub} 的 equals / hashCode / toString
 * 与 {@code AbstractLicense} 的日期防御性拷贝。
 */
@DisplayName("许可证值语义")
class LicenseValueSemanticsTest {

    @Test
    @DisplayName("全部字段相同时相等且哈希一致")
    void equalFieldsMakeEqualLicenses() {
        V4License first = sample();
        V4License second = sample();

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isEqualTo(first);
        assertThat(first).isNotEqualTo("not-a-license");
        assertThat(first).isNotEqualTo(null);
    }

    @Test
    @DisplayName("任一字段不同都不相等")
    void anyDifferentFieldBreaksEquality() {
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setConsumerAmount(9)));
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setConsumerType("Computer")));
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setInfo("other")));
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setSubject("CN=Other")));
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setIssued(new Date(1_001L))));
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setNotAfter(new Date(9_998L))));
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setNotBefore(new Date(9_997L))));
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setHolder(new X500Principal("CN=Other"))));
        assertThat(sample()).isNotEqualTo(withBean(bean -> bean.setIssuer(new X500Principal("CN=Other"))));
    }

    @Test
    @DisplayName("canEqual 为 false 的子类不相等")
    void canEqualGuardsEquality() {
        OtherLicense other = new OtherLicense();
        other.setSubject("CN=Swak Test");

        assertThat(sample()).isNotEqualTo(other);
    }

    @Test
    @DisplayName("日期属性返回副本，修改返回值不影响内部状态")
    void datePropertiesAreDefensivelyCopied() {
        V4License license = new V4License();
        Date issued = new Date(1_000_000L);

        license.setIssued(issued);
        issued.setTime(2_000_000L);
        assertThat(license.getIssued()).isNotSameAs(issued);
        assertThat(license.getIssued().getTime()).isEqualTo(1_000_000L);

        Date returned = license.getIssued();
        returned.setTime(3_000_000L);
        assertThat(license.getIssued().getTime()).isEqualTo(1_000_000L);

        license.setNotAfter(null);
        assertThat(license.getNotAfter()).isNull();
        license.setNotBefore(null);
        assertThat(license.getNotBefore()).isNull();
    }

    @Test
    @DisplayName("toString 含全部属性，字符串值带引号并转义")
    void toStringIncludesProperties() {
        V4License license = sample();
        license.setInfo("a\"b\\c");

        assertThat(license.toString())
                .contains("subject=\"CN=Swak Test\"")
                .contains("consumerType=\"User\"")
                .contains("consumerAmount=2")
                .contains("a\\\"b\\\\c");

        V4License empty = new V4License();
        assertThat(empty.toString()).contains("subject=null").contains("holder=null");
    }

    @Test
    @DisplayName("extra 属性参与相等性比较")
    void extraParticipatesInEquality() {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("k", "v");

        V4License withExtra = sample();
        withExtra.setExtra(extra);

        assertThat(sample()).isNotEqualTo(withExtra);

        V4License sameExtra = sample();
        sameExtra.setExtra(new LinkedHashMap<>(extra));
        assertThat(withExtra).isEqualTo(sameExtra).hasSameHashCodeAs(sameExtra);
    }

    private static V4License sample() {
        V4License license = new V4License();
        license.setConsumerAmount(2);
        license.setConsumerType("User");
        license.setHolder(new X500Principal("CN=Holder"));
        license.setIssuer(new X500Principal("CN=Issuer"));
        license.setSubject("CN=Swak Test");
        license.setInfo("info");
        license.setIssued(new Date(1_000L));
        license.setNotBefore(new Date(1_000L));
        license.setNotAfter(new Date(9_999L));
        return license;
    }

    private static V4License withBean(java.util.function.Consumer<V4License> mutator) {
        V4License license = sample();
        mutator.accept(license);
        return license;
    }

    private static final class OtherLicense extends V4License {

        @Override
        public boolean canEqual(Object that) {
            return false;
        }
    }
}
