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
import org.junit.jupiter.api.io.TempDir;

import javax.security.auth.x500.X500Principal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 链式消费者许可证管理器（{@code ChainedLicenseManager}）回归：
 * 优先使用父管理器，父失败时回退到自己的存储，再失败则自动生成试用许可证。
 * <p>
 * 试用许可证需要管理器具备<b>签发</b>能力（{@code canGenerateLicenseKeys()} 会用私钥试签一次），
 * 因此这部分用例使用 {@code license/swak-notary.p12}（别名 {@code swak}）作为凭据。
 */
@DisplayName("链式许可证管理器")
class ChainedLicenseManagerTest {

    private static final String SUBJECT = "CN=Swak Test";
    private static final String ALIAS = "swak";
    private static final String KEYSTORE = "license/swak-notary.p12";
    private static final char[] PASSWORD = "Password123".toCharArray();
    private static final PasswordProtection PROTECTION =
            new ObfuscatedPasswordProtection(new ObfuscatedString(ObfuscatedString.array(new String(PASSWORD))));
    private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");

    private static final KeyPair KEY_PAIR = generateKeyPair();
    private static final Source PRIVATE_KEY = keySource("PRIVATE KEY", KEY_PAIR.getPrivate().getEncoded());
    private static final Source PUBLIC_KEY = keySource("PUBLIC KEY", KEY_PAIR.getPublic().getEncoded());

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("父管理器已有许可证时直接沿用，不写子存储")
    void delegatesToParent() throws Exception {
        LicenseManagementContext context = context();
        Store vendorStore = BIOS.memory();
        vendor(context).generateKeyFrom(license(context)).saveTo(vendorStore);

        Store parentStore = BIOS.memory();
        ConsumerLicenseManager parent = consumer(context, parentStore);
        parent.install(vendorStore);

        Store childStore = BIOS.memory();
        ConsumerLicenseManager chained = chained(context, childStore, parent);

        assertThat(chained.load().getSubject()).isEqualTo(SUBJECT);
        assertThat(chained.verify().getSubject()).isEqualTo(SUBJECT);
        assertThat(childStore.exists()).isFalse();
    }

    @Test
    @DisplayName("父子都没有许可证且具备签发能力时，自动生成试用许可证到子存储")
    void generatesTrialKeyWhenNothingInstalled() throws Exception {
        LicenseManagementContext signing = signingContext();
        ConsumerLicenseManager parent = consumer(context(), BIOS.memory());

        Store childStore = BIOS.memory();
        ConsumerLicenseManager chained = chainedWithKeystore(signing, childStore, parent);

        License generated = chained.load();

        assertThat(generated).isNotNull();
        assertThat(generated.getSubject()).isEqualTo(SUBJECT);
        assertThat(generated.getConsumerAmount()).isEqualTo(1);
        assertThat(generated.getNotAfter()).isNotNull();
        assertThat(childStore.exists()).isTrue();
    }

    @Test
    @DisplayName("试用许可证生成一次后再次加载复用存储内容")
    void reusesGeneratedTrialKey() throws Exception {
        LicenseManagementContext signing = signingContext();
        ConsumerLicenseManager chained =
                chainedWithKeystore(signing, BIOS.memory(), consumer(context(), BIOS.memory()));

        License first = chained.load();
        License second = chained.load();

        assertThat(first.getIssued()).isEqualTo(second.getIssued());
        assertThat(second.getSubject()).isEqualTo(SUBJECT);
    }

    @Test
    @DisplayName("只有公钥的消费者无法签发试用许可证，原样抛出父级失败原因")
    void publicKeyOnlyConsumerCannotGenerateTrial() throws Exception {
        LicenseManagementContext context = context();
        Store childStore = BIOS.memory();
        ConsumerLicenseManager chained = chained(context, childStore, consumer(context, BIOS.memory()));

        assertThatThrownBy(chained::load)
                .isInstanceOf(LicenseManagementException.class)
                .hasRootCauseInstanceOf(com.swak.license.api.io.NoContentException.class);
        assertThat(childStore.exists()).isFalse();
    }

    @Test
    @DisplayName("安装与卸载都委托给父管理器")
    void installAndUninstallDelegateToParent() throws Exception {
        LicenseManagementContext context = context();
        Store vendorStore = BIOS.memory();
        vendor(context).generateKeyFrom(license(context)).saveTo(vendorStore);

        Store parentStore = BIOS.memory();
        ConsumerLicenseManager parent = consumer(context, parentStore);
        Store childStore = BIOS.memory();
        ConsumerLicenseManager chained = chained(context, childStore, parent);

        chained.install(vendorStore);
        assertThat(parentStore.exists()).isTrue();
        assertThat(childStore.exists()).isFalse();

        chained.uninstall();
        assertThat(parentStore.exists()).isFalse();
    }

    @Test
    @DisplayName("storeInPath 会把生成的试用许可证写到指定文件")
    void storeInPathWritesFile() throws Exception {
        Path path = tempDir.resolve("generated.license");

        ConsumerLicenseManager chained = signingContext().consumer()
                .storeInPath(path)
                .authentication().alias(ALIAS).storeProtection(PROTECTION).keyProtection(PROTECTION)
                .loadFrom(keystoreSource()).up()
                .encryption().protection(PROTECTION).up()
                .ftpDays(30)
                .parent(consumer(context(), BIOS.memory()))
                .build();

        assertThat(chained.load()).isNotNull();
        assertThat(Files.exists(path)).isTrue();
        // 许可证内容经过压缩与加密，是二进制而非文本，只断言非空。
        assertThat(Files.size(path)).isGreaterThan(0L);
    }

    private static LicenseManagementContext context() {
        return V4.builder()
                .subject(SUBJECT)
                .clock(Clock.fixed(NOW, ZoneOffset.UTC))
                .build();
    }

    /**
     * 具备签发能力的上下文：keystore 中同时有私钥与证书，可完成 sign 与 verify。
     */
    private static LicenseManagementContext signingContext() {
        return V4.builder()
                .subject(SUBJECT)
                .clock(Clock.fixed(NOW, ZoneOffset.UTC))
                .authenticationFactory(Notary::new)
                .keystoreType("PKCS12")
                .build();
    }

    private static Source keystoreSource() {
        return BIOS.resource(KEYSTORE);
    }

    private static ConsumerLicenseManager chainedWithKeystore(LicenseManagementContext context, Store store,
                                                              ConsumerLicenseManager parent) {
        return context.consumer()
                .storeIn(store)
                .authentication().alias(ALIAS).storeProtection(PROTECTION).keyProtection(PROTECTION)
                .loadFrom(keystoreSource()).up()
                .encryption().protection(PROTECTION).up()
                .ftpDays(30)
                .parent(parent)
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

    private static VendorLicenseManager vendor(LicenseManagementContext context) {
        return context.vendor()
                .authentication().alias("swak").storeProtection(PROTECTION).loadFrom(PRIVATE_KEY).up()
                .encryption().protection(PROTECTION).up()
                .build();
    }

    private static ConsumerLicenseManager consumer(LicenseManagementContext context, Store store) {
        return context.consumer()
                .storeIn(store)
                .authentication().alias("swak").storeProtection(PROTECTION).loadFrom(PUBLIC_KEY).up()
                .encryption().protection(PROTECTION).up()
                .build();
    }

    private static ConsumerLicenseManager chained(LicenseManagementContext context, Store store,
                                                  ConsumerLicenseManager parent) {
        return context.consumer()
                .storeIn(store)
                .authentication().alias("swak").storeProtection(PROTECTION).loadFrom(PUBLIC_KEY).up()
                .encryption().protection(PROTECTION).up()
                .parent(parent)
                .build();
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Source keySource(String type, byte[] encoded) {
        try {
            String pem = "-----BEGIN " + type + "-----\n"
                    + Base64.getMimeEncoder().encodeToString(encoded)
                    + "\n-----END " + type + "-----\n";
            Store store = BIOS.memory();
            store.content(pem.getBytes(StandardCharsets.US_ASCII));
            return store;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
