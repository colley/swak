package com.swak.license.core.auth;

import com.swak.license.api.io.Socket;

import javax.crypto.Cipher;
import java.io.*;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAEncrypt {
    /**
     * 字节数据转字符串专用集合
     */
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String loadKey(InputStream inputStream) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return doReader(br);
        } catch (Exception e) {
            throw new Exception("loadKeyByFile error");
        }
    }

    public static RSAPublicKey loadPublicKey(Socket<InputStream> inputStream) throws Exception {
        return loadPublicKeyByStr(loadKey(inputStream.get()));
    }

    public static RSAPrivateKey loadPrivateKey(Socket<InputStream> inputStream) throws Exception {
        return loadPrivateKeyByStr(loadKey(inputStream.get()));
    }

    private static String doReader(BufferedReader reader) throws IOException {
        String readLine;
        StringBuilder sb = new StringBuilder();
        while ((readLine = reader.readLine()) != null) {
            if (readLine.charAt(0) == '-') {
                continue;
            }
            sb.append(readLine);
            sb.append('\r');
        }
        return sb.toString();
    }


    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr 公钥数据字符串
     */
    public static RSAPublicKey loadPublicKeyByStr(String publicKeyStr) throws Exception {
        try {
            byte[] buffer = Base64.getMimeDecoder().decode(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("No Such Algorithm Exception");
        } catch (InvalidKeySpecException  e) {
            throw new Exception("Invalid KeySpec Exception");
        }
    }



    public static RSAPrivateKey loadPrivateKeyByStr(String privateKeyStr) throws Exception {
        try {
            byte[] buffer = Base64.getMimeDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 公钥加密过程
     *
     * @param publicKey     公钥
     * @param plainTextData 明文数据
     * @return
     */
    public static byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData) throws Exception {
        if (publicKey == null) {
            throw new Exception("publicKey is null");
        }
        return doCipherByte(Cipher.ENCRYPT_MODE, publicKey, plainTextData);
    }

    /**
     * 私钥加密过程
     *
     * @param privateKey    私钥
     * @param plainTextData 明文数据
     * @return
     */
    public static byte[] encrypt(RSAPrivateKey privateKey, byte[] plainTextData) throws Exception {
        if (privateKey == null) {
            throw new Exception("privateKey is null");
        }
        return doCipherByte(Cipher.ENCRYPT_MODE, privateKey, plainTextData);
    }

    public static Cipher getCipher4Encryption(Key key) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static Cipher getCipher4Decryption(Key key) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 私钥解密过程
     *
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @return 明文
     */
    public static byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData) throws Exception {
        if (privateKey == null) {
            throw new Exception("privateKey is null");
        }
        return doCipherByte(Cipher.DECRYPT_MODE, privateKey, cipherData);
    }

    private static byte[] doCipherByte(int mode, Key key, byte[] data) throws Exception {
        try {
            // 使用默认RSA
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(mode, key);
            byte[] output = cipher.doFinal(data);
            return output;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 公钥解密过程
     *
     * @param publicKey  公钥
     * @param cipherData 密文数据
     * @return 明文
     */
    public static byte[] decrypt(RSAPublicKey publicKey, byte[] cipherData) throws Exception {
        if (publicKey == null) {
            throw new Exception("publicKey is null");
        }
        return doCipherByte(Cipher.DECRYPT_MODE, publicKey, cipherData);
    }

    /**
     * 字节数据转十六进制字符串
     *
     * @param data 输入数据
     * @return 十六进制内容
     */
    public static String byteArrayToString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            // 取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0) >>> 4]);
            // 取出字节的低四位 作为索引得到相应的十六进制标识符
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
            if (i < data.length - 1) {
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.toString();
    }
}