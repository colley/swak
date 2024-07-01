
package com.swak.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Blowfish 加解密
 * @author colley.ma
 * @since 2022/9/9 18:41
 */
@Slf4j
public class BlowfishCipherHelper {
	private static byte[] IV = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

	public static String encrypt(String content, String secretKey) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(StringPool.UTF8), "Blowfish");
			Cipher cipher = Cipher.getInstance("Blowfish/CFB64/NoPadding"); // "算法/模式/补码方式"
			IvParameterSpec ivps = new IvParameterSpec(IV); // 使用CFB64模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivps);
			byte[] original = cipher.doFinal(content.getBytes(StringPool.UTF8));
			return Base64Util.encode2Str(original,StringPool.UTF8,true);
		} catch (Exception e) {
			log.error("[swak-Blowfish] -encrypt error",e);
			e.printStackTrace();
		}

		return null;
	}

	public static String decrypt(String encrypted, String secretKey) {
		try {
			byte[] encrypted1 = Base64Util.decode(encrypted,StringPool.UTF8,true);
			SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(StringPool.UTF8), "Blowfish");
			Cipher cipher = Cipher.getInstance("Blowfish/CFB64/NoPadding"); // "算法/模式/补码方式"
			IvParameterSpec ivps = new IvParameterSpec(IV); // 使用CFB64模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivps);
			byte[] original = cipher.doFinal(encrypted1);
			return StringUtils.newStringUtf8(original);
		} catch (Exception e) {
			log.error("decrypt error",e);
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static String encrypt(String content) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(StringPool.KEY.getBytes(StringPool.UTF8), "Blowfish");
			Cipher cipher = Cipher.getInstance("Blowfish/CFB64/NoPadding"); // "算法/模式/补码方式"
			IvParameterSpec ivps = new IvParameterSpec(IV); // 使用CFB64模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivps);
			byte[] original = cipher.doFinal(content.getBytes(StringPool.UTF8));
			String encodeStr = Base64Util.encode2Str(original, StringPool.UTF8, true);
			//去掉换行符 base64超过76字符自动加换行符
			encodeStr = encodeStr.replaceAll("\r\n", "");
			return encodeStr;
		} catch (Exception e) {
			log.error("encrypt error",e);
			e.printStackTrace();
		}
		return null;
	}

	public static String decrypt(String encrypted) {
		try {
			byte[] encrypted1 = Base64Util.decode(encrypted,StringPool.UTF8,true);
			SecretKeySpec skeySpec = new SecretKeySpec(StringPool.KEY.getBytes(StringPool.UTF8), "Blowfish");
			Cipher cipher = Cipher.getInstance("Blowfish/CFB64/NoPadding"); // "算法/模式/补码方式"
			IvParameterSpec ivps = new IvParameterSpec(IV); // 使用CFB64模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivps);
			byte[] original = cipher.doFinal(encrypted1);
			return StringUtils.newStringUtf8(original);
		} catch (Exception e) {
			log.error("decrypt error",e);
			return null;
		}
	}
}
