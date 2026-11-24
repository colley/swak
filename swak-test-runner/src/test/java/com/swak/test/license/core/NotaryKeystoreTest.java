package com.swak.test.license.core;

import com.swak.common.util.ObfuscatedString;
import com.swak.license.api.ConsumerLicenseManager;
import com.swak.license.api.License;
import com.swak.license.api.LicenseManagementContext;
import com.swak.license.api.LicenseManagementException;
import com.swak.license.api.VendorLicenseManager;
import com.swak.license.api.io.Source;
import com.swak.license.api.io.Store;
import com.swak.license.api.io.bios.BIOS;
import com.swak.license.api.passwd.PasswordProtection;
import com.swak.license.core.auth.Notary;
import com.swak.license.core.passwd.ObfuscatedPasswordProtection;
import com.swak.license.provider.V4;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.security.auth.x500.X500Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 基于 PKCS12 keystore 的公证人（{@link Notary}）回归。
 * <p>
 * 夹具 {@code license/swak-notary.p12} 是自签名的 RSA 证书 + 私钥，别名 {@code swak}，
 * 口令与 {@link #PASSWORD} 一致。
 */
@DisplayName("Keystore 公证人")
class NotaryKeystoreTest {

    private static final String SUBJECT = "CN=Swak Test";
    private static final String ALIAS = "swak";
    private static final String KEYSTORE = "license/swak-notary.p12";
    private static final char[] PASSWORD = "Password123".toCharArray();
    private static final PasswordProtection PROTECTION =
            new ObfuscatedPasswordProtection(new ObfuscatedString(ObfuscatedString.array(new String(PASSWORD))));
    private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");

    @Test
    @DisplayName("keystore 私钥签发、证书公钥校验可跑通全链路")
    void signsAndVerifiesThroughKeystore() throws Exception {
        LicenseManagementContext context = context();

        Store vendorStore = BIOS.memory();
        vendor(context, ALIAS, PROTECTION).generateKeyFrom(license(context)).saveTo(vendorStore);

        Store consumerStore = BIOS.memory();
        ConsumerLicenseManager consumer = consumer(context, consumerStore, PROTECTION);
        consumer.install(vendorStore);

        License loaded = consumer.load();
        License verified = consumer.verify();

        assertThat(loaded.getSubject()).isEqualTo(SUBJECT);
        assertThat(verified.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    @DisplayName("配置签名算法时使用配置值")
    void configuredAlgorithmIsUsed() throws Exception {
        LicenseManagementContext context = context();

        Store vendorStore = BIOS.memory();
        context.vendor()
                .authentication().alias(ALIAS).storeProtection(PROTECTION).keyProtection(PROTECTION)
                .algorithm("SHA256withRSA").loadFrom(keystoreSource()).up()
                .encryption().protection(PROTECTION).up()
                .build()
                .generateKeyFrom(license(context))
                .saveTo(vendorStore);

        assertThat(vendorStore.exists()).isTrue();
    }

    @Test
    @DisplayName("别名不存在时抛出 NotaryException")
    void missingAliasFails() throws Exception {
        LicenseManagementContext context = context();
        Store vendorStore = BIOS.memory();
        vendor(context, ALIAS, PROTECTION).generateKeyFrom(license(context)).saveTo(vendorStore);

        ConsumerLicenseManager consumer = consumer(context, BIOS.memory(), PROTECTION, "missing");

        assertThatThrownBy(() -> consumer.install(vendorStore)).isInstanceOf(LicenseManagementException.class);
    }

    @Test
    @DisplayName("keystore 口令错误时无法加载")
    void wrongKeystorePasswordFails() throws Exception {
        LicenseManagementContext context = context();
        Store vendorStore = BIOS.memory();
        vendor(context, ALIAS, PROTECTION).generateKeyFrom(license(context)).saveTo(vendorStore);

        PasswordProtection wrong = new ObfuscatedPasswordProtection(
                new ObfuscatedString(ObfuscatedString.array("WrongPass123")));
        ConsumerLicenseManager consumer = consumer(context, BIOS.memory(), wrong);

        assertThatThrownBy(() -> consumer.install(vendorStore)).isInstanceOf(Exception.class);
    }

    private static LicenseManagementContext context() {
        return V4.builder()
                .subject(SUBJECT)
                .clock(Clock.fixed(NOW, ZoneOffset.UTC))
                .authenticationFactory(Notary::new)
                .keystoreType("PKCS12")
                .build();
    }

    private static License license(LicenseManagementContext context) {
        License license = context.licenseFactory().license();
        license.setConsumerAmount(1);
        license.setConsumerType("User");
        license.setHolder(new X500Principal("CN=Holder"));
        license.setIssued(Date.from(NOW));
        license.setNotBefore(Date.from(NOW));
        license.setNotAfter(Date.from(NOW.plusSeconds(86_400)));
        return license;
    }

    private static VendorLicenseManager vendor(LicenseManagementContext context, String alias,
                                               PasswordProtection protection) {
        return context.vendor()
                .authentication().alias(alias).storeProtection(protection).keyProtection(protection)
                .loadFrom(keystoreSource()).up()
                .encryption().protection(protection).up()
                .build();
    }

    private static ConsumerLicenseManager consumer(LicenseManagementContext context, Store store,
                                                   PasswordProtection protection) {
        return consumer(context, store, protection, ALIAS);
    }

    private static ConsumerLicenseManager consumer(LicenseManagementContext context, Store store,
                                                   PasswordProtection protection, String alias) {
        return context.consumer()
                .storeIn(store)
                .authentication().alias(alias).storeProtection(protection).keyProtection(protection)
                .loadFrom(keystoreSource()).up()
                .encryption().protection(protection).up()
                .build();
    }

    private static Source keystoreSource() {
        return BIOS.resource(KEYSTORE);
    }
}
