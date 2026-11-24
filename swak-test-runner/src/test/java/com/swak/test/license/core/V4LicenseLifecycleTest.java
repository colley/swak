package com.swak.test.license.core;

import com.swak.common.util.ObfuscatedString;
import com.swak.license.api.ConsumerLicenseManager;
import com.swak.license.api.License;
import com.swak.license.api.LicenseFunctionComposition;
import com.swak.license.api.LicenseManagementContext;
import com.swak.license.api.LicenseManagementException;
import com.swak.license.api.LicenseValidationException;
import com.swak.license.api.VendorLicenseManager;
import com.swak.license.api.io.Source;
import com.swak.license.api.io.Store;
import com.swak.license.api.io.bios.BIOS;
import com.swak.license.api.passwd.PasswordProtection;
import com.swak.license.core.passwd.ObfuscatedPasswordProtection;
import com.swak.license.provider.V4;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.security.auth.x500.X500Principal;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * V4 许可证端到端生命周期回归：签发、安装、加载、校验、卸载。
 * <p>
 * 签名密钥对在测试内即时生成，用 {@code Source} 直接喂 PEM，不依赖 keystore 夹具。
 */
@DisplayName("V4 许可证生命周期")
class V4LicenseLifecycleTest {

    private static final String SUBJECT = "CN=Swak Test";
    private static final char[] PASSWORD = "Password123".toCharArray();
    private static final PasswordProtection PROTECTION =
            new ObfuscatedPasswordProtection(new ObfuscatedString(ObfuscatedString.array(new String(PASSWORD))));

    private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");

    private static Source privateKeySource;
    private static Source publicKeySource;

    @BeforeAll
    static void generateKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        privateKeySource = sourceOf(pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
        publicKeySource = sourceOf(pem("PUBLIC KEY", pair.getPublic().getEncoded()));
    }

    @Test
    @DisplayName("签发到校验全链路：安装、加载、校验都拿到同一份许可证")
    void fullLifecycle() throws Exception {
        LicenseManagementContext context = context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC));

        Store vendorStore = BIOS.memory();
        License license = newLicense(context);
        vendor(context, privateKeySource)
                .generateKeyFrom(license)
                .saveTo(vendorStore);

        assertThat(vendorStore.exists()).isTrue();
        assertThat(new String(vendorStore.content(), StandardCharsets.UTF_8)).isNotBlank();

        Store consumerStore = BIOS.memory();
        ConsumerLicenseManager consumer = consumer(context, consumerStore, publicKeySource);

        License installed = consumer.install(vendorStore);
        License loaded = consumer.load();
        License verified = consumer.verify();

        assertThat(installed.getSubject()).isEqualTo(SUBJECT);
        assertThat(loaded.getSubject()).isEqualTo(SUBJECT);
        assertThat(verified.getSubject()).isEqualTo(SUBJECT);
        assertThat(loaded.getHolder()).isEqualTo(license.getHolder());
        assertThat(loaded.getConsumerAmount()).isEqualTo(license.getConsumerAmount());
        assertThat(loaded.getConsumerType()).isEqualTo(license.getConsumerType());
        assertThat(loaded.getInfo()).isEqualTo(license.getInfo());
        assertThat(loaded.getIssued()).isEqualTo(license.getIssued());
    }

    @Test
    @DisplayName("生成器可回读已签发内容，extra 自定义属性随许可证往返")
    void generatedKeyCanBeReadBack() throws Exception {
        LicenseManagementContext context = context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC));
        Store store = BIOS.memory();

        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("customer", "ACME");
        License license = newLicense(context);
        license.setExtra(extra);

        License roundTrip = vendor(context, privateKeySource)
                .generateKeyFrom(license)
                .saveTo(store)
                .license();

        assertThat(roundTrip.getExtra()).isEqualTo(extra);
        assertThat(roundTrip.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    @DisplayName("卸载：删除存储内容，之后加载失败")
    void uninstallRemovesStoredKey() throws Exception {
        LicenseManagementContext context = context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC));
        Store vendorStore = BIOS.memory();
        vendor(context, privateKeySource).generateKeyFrom(newLicense(context)).saveTo(vendorStore);

        Store consumerStore = BIOS.memory();
        ConsumerLicenseManager consumer = consumer(context, consumerStore, publicKeySource);
        consumer.install(vendorStore);

        consumer.uninstall();

        assertThat(consumerStore.exists()).isFalse();
        assertThatThrownBy(consumer::load).isInstanceOf(LicenseManagementException.class);
    }

    @Test
    @DisplayName("篡改内容后校验失败")
    void tamperedKeyFailsVerification() throws Exception {
        LicenseManagementContext context = context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC));
        Store vendorStore = BIOS.memory();
        vendor(context, privateKeySource).generateKeyFrom(newLicense(context)).saveTo(vendorStore);

        byte[] content = vendorStore.content();
        content[content.length / 2] = (byte) (content[content.length / 2] == 'A' ? 'B' : 'A');
        vendorStore.content(content);

        Store consumerStore = BIOS.memory();
        ConsumerLicenseManager consumer = consumer(context, consumerStore, publicKeySource);
        assertThatThrownBy(() -> consumer.install(vendorStore)).isInstanceOf(LicenseManagementException.class);
    }

    @Test
    @DisplayName("校验：许可证已过期时抛出 LicenseValidationException")
    void expiredLicenseRejected() throws Exception {
        LicenseManagementContext signing = context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC));
        Store vendorStore = BIOS.memory();
        License license = newLicense(signing);
        license.setNotAfter(Date.from(NOW.plusSeconds(3600)));
        vendor(signing, privateKeySource).generateKeyFrom(license).saveTo(vendorStore);

        Store consumerStore = BIOS.memory();
        LicenseManagementContext later = context(SUBJECT, Clock.fixed(NOW.plusSeconds(7200), ZoneOffset.UTC));
        ConsumerLicenseManager consumer = consumer(later, consumerStore, publicKeySource);
        consumer.install(vendorStore);

        assertThatThrownBy(consumer::verify)
                .isInstanceOf(LicenseValidationException.class)
                .hasMessageContaining("2026-06-01");
    }

    @Test
    @DisplayName("校验：许可证尚未生效时被拒绝")
    void notYetValidLicenseRejected() throws Exception {
        // 签发时钟设在该许可证生效之后，否则签发阶段的校验就会失败。
        LicenseManagementContext signing = context(SUBJECT, Clock.fixed(NOW.plusSeconds(7200), ZoneOffset.UTC));
        Store vendorStore = BIOS.memory();
        License license = newLicense(signing);
        license.setIssued(Date.from(NOW));
        license.setNotBefore(Date.from(NOW.plusSeconds(3600)));
        license.setNotAfter(Date.from(NOW.plusSeconds(10800)));
        vendor(signing, privateKeySource).generateKeyFrom(license).saveTo(vendorStore);

        Store consumerStore = BIOS.memory();
        ConsumerLicenseManager consumer = consumer(context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC)),
                consumerStore, publicKeySource);
        consumer.install(vendorStore);

        assertThatThrownBy(consumer::verify)
                .isInstanceOf(LicenseValidationException.class)
                .hasMessageContaining("2026-06-01");
    }

    @Test
    @DisplayName("校验：subject 与上下文不一致时被拒绝")
    void subjectMismatchRejected() throws Exception {
        LicenseManagementContext signing = context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC));
        Store vendorStore = BIOS.memory();
        vendor(signing, privateKeySource).generateKeyFrom(newLicense(signing)).saveTo(vendorStore);

        Store consumerStore = BIOS.memory();
        ConsumerLicenseManager consumer = consumer(context("CN=Other Subject", Clock.fixed(NOW, ZoneOffset.UTC)),
                consumerStore, publicKeySource);
        consumer.install(vendorStore);

        assertThatThrownBy(consumer::verify)
                .isInstanceOf(LicenseValidationException.class)
                .hasMessageContaining("Other Subject");
    }

    @Test
    @DisplayName("签发校验：非法许可证在保存时被拒绝")
    void invalidLicenseRejectedOnSave() throws Exception {
        LicenseManagementContext context = context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC));

        License negativeAmount = newLicense(context);
        negativeAmount.setConsumerAmount(-1);
        assertThatThrownBy(() -> vendor(context, privateKeySource)
                .generateKeyFrom(negativeAmount)
                .saveTo(BIOS.memory()))
                .isInstanceOf(LicenseManagementException.class);
    }

    @Test
    @DisplayName("未配置初始化时，缺失字段由内置初始化补齐")
    void initializationFillsDefaults() throws Exception {
        LicenseManagementContext context = context(SUBJECT, Clock.fixed(NOW, ZoneOffset.UTC));
        Store store = BIOS.memory();

        License bare = context.licenseFactory().license();
        License roundTrip = vendor(context, privateKeySource)
                .generateKeyFrom(bare)
                .saveTo(store)
                .license();

        assertThat(roundTrip.getConsumerAmount()).isEqualTo(1);
        assertThat(roundTrip.getConsumerType()).isEqualTo("User");
        assertThat(roundTrip.getHolder()).isNotNull();
        assertThat(roundTrip.getIssued()).isNotNull();
        assertThat(roundTrip.getIssuer()).isNotNull();
        assertThat(roundTrip.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    @DisplayName("自定义初始化 + override 组合时不再补默认值，缺失字段导致校验失败")
    void customInitializationCanSkipDefaults() {
        LicenseManagementContext context = V4.builder()
                .subject(SUBJECT)
                .clock(Clock.fixed(NOW, ZoneOffset.UTC))
                .initialization(bean -> {
                })
                .initializationComposition(LicenseFunctionComposition.override)
                .build();

        License bare = context.licenseFactory().license();
        bare.setConsumerAmount(1);

        assertThatThrownBy(() -> vendor(context, privateKeySource)
                .generateKeyFrom(bare)
                .saveTo(BIOS.memory()))
                .isInstanceOf(LicenseValidationException.class);
    }

    private static LicenseManagementContext context(String subject, Clock clock) {
        return V4.builder()
                .subject(subject)
                .clock(clock)
                .build();
    }

    private static License newLicense(LicenseManagementContext context) {
        License license = context.licenseFactory().license();
        license.setConsumerAmount(2);
        license.setConsumerType("User");
        license.setHolder(new X500Principal("CN=Holder"));
        license.setIssued(Date.from(NOW));
        license.setNotBefore(Date.from(NOW));
        license.setNotAfter(Date.from(NOW.plusSeconds(86_400)));
        license.setInfo("swak test license");
        return license;
    }

    private static VendorLicenseManager vendor(LicenseManagementContext context, Source privateKey) {
        return context.vendor()
                .authentication().alias("swak").storeProtection(PROTECTION).loadFrom(privateKey).up()
                .encryption().protection(PROTECTION).up()
                .build();
    }

    private static ConsumerLicenseManager consumer(LicenseManagementContext context, Store store, Source publicKey) {
        return context.consumer()
                .storeIn(store)
                .authentication().alias("swak").storeProtection(PROTECTION).loadFrom(publicKey).up()
                .encryption().protection(PROTECTION).up()
                .build();
    }

    private static Source sourceOf(String pem) throws Exception {
        Store store = BIOS.memory();
        store.content(pem.getBytes(StandardCharsets.US_ASCII));
        return store;
    }

    private static String pem(String type, byte[] encoded) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder().encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
    }
}
