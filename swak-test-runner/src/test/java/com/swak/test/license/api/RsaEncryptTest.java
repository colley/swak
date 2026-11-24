package com.swak.test.license.api;

import com.swak.license.core.auth.RSAEncrypt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RSA 密钥加载与加解密回归。密钥对在测试内即时生成，不依赖外部夹具。
 */
@DisplayName("RSA 加解密")
class RsaEncryptTest {

    private static final byte[] DATA = "swak-license-rsa".getBytes(StandardCharsets.UTF_8);

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    private static String publicPem;
    private static String privatePem;

    @BeforeAll
    static void generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        publicKey = (RSAPublicKey) pair.getPublic();
        privateKey = (RSAPrivateKey) pair.getPrivate();
        publicPem = pem("PUBLIC KEY", publicKey.getEncoded());
        privatePem = pem("PRIVATE KEY", privateKey.getEncoded());
    }

    @Test
    @DisplayName("去掉 PEM 头尾行后可加载公私钥，公钥加密私钥解密可还原")
    void loadsKeysFromPemText() throws Exception {
        // 注意：Base64 的 MIME 解码器会忽略 '-' 等分隔符但保留 BEGIN/END 里的字母，
        // 因此必须先经 loadKey 剥离头尾行，这也是 loadPublicKey/loadPrivateKey 的内部做法。
        RSAPublicKey loadedPublic = RSAEncrypt.loadPublicKeyByStr(base64Body(publicPem));
        RSAPrivateKey loadedPrivate = RSAEncrypt.loadPrivateKeyByStr(base64Body(privatePem));

        assertThat(loadedPublic.getModulus()).isEqualTo(publicKey.getModulus());
        assertThat(loadedPrivate.getModulus()).isEqualTo(privateKey.getModulus());

        byte[] encrypted = RSAEncrypt.encrypt(loadedPublic, DATA);
        assertThat(encrypted).isNotEqualTo(DATA);
        assertThat(RSAEncrypt.decrypt(loadedPrivate, encrypted)).isEqualTo(DATA);
    }

    @Test
    @DisplayName("私钥加密公钥解密同样可往返")
    void privateEncryptPublicDecrypt() throws Exception {
        byte[] encrypted = RSAEncrypt.encrypt(privateKey, DATA);

        assertThat(RSAEncrypt.decrypt(publicKey, encrypted)).isEqualTo(DATA);
    }

    @Test
    @DisplayName("loadKey 跳过 PEM 头尾行，Socket 入口可直接加载密钥")
    void loadsKeysThroughStreams() throws Exception {
        String loaded = RSAEncrypt.loadKey(new ByteArrayInputStream(publicPem.getBytes(StandardCharsets.US_ASCII)));

        assertThat(loaded).doesNotContain("-----BEGIN");
        assertThat(loaded).contains("MII");

        RSAPublicKey viaSocket = RSAEncrypt.loadPublicKey(() -> new ByteArrayInputStream(
                publicPem.getBytes(StandardCharsets.US_ASCII)));
        RSAPrivateKey viaSocketPrivate = RSAEncrypt.loadPrivateKey(() -> new ByteArrayInputStream(
                privatePem.getBytes(StandardCharsets.US_ASCII)));

        assertThat(viaSocket.getModulus()).isEqualTo(publicKey.getModulus());
        assertThat(viaSocketPrivate.getModulus()).isEqualTo(privateKey.getModulus());
    }

    @Test
    @DisplayName("预置 Cipher 可直接用于加解密")
    void ciphersAreInitialized() throws Exception {
        Cipher encryptor = RSAEncrypt.getCipher4Encryption(publicKey);
        byte[] encrypted = encryptor.doFinal(DATA);

        Cipher decryptor = RSAEncrypt.getCipher4Decryption(privateKey);
        assertThat(decryptor.doFinal(encrypted)).isEqualTo(DATA);
    }

    @Test
    @DisplayName("空密钥与非法密钥文本被拒绝")
    void rejectsInvalidKeys() {
        assertThatThrownBy(() -> RSAEncrypt.encrypt((RSAPublicKey) null, DATA))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("publicKey is null");
        assertThatThrownBy(() -> RSAEncrypt.encrypt((RSAPrivateKey) null, DATA))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("privateKey is null");
        assertThatThrownBy(() -> RSAEncrypt.decrypt((RSAPublicKey) null, DATA))
                .isInstanceOf(Exception.class);
        assertThatThrownBy(() -> RSAEncrypt.decrypt((RSAPrivateKey) null, DATA))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> RSAEncrypt.loadPublicKeyByStr("not a key"))
                .isInstanceOf(Exception.class);
        assertThatThrownBy(() -> RSAEncrypt.loadPrivateKeyByStr("not a key"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("字节数组转十六进制以空格分隔且补零")
    void formatsHex() {
        assertThat(RSAEncrypt.byteArrayToString(new byte[]{0x0a, (byte) 0xff, 0x00})).isEqualTo("0a ff 00");
        assertThat(RSAEncrypt.byteArrayToString(new byte[0])).isEmpty();
    }

    private static String pem(String type, byte[] encoded) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder().encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
    }

    private static String base64Body(String pem) throws Exception {
        return RSAEncrypt.loadKey(new ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII)));
    }
}
