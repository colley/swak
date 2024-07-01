
package com.swak.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
public final class DigestUtils {

	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	public static String md5(String s) {
		return md5(s.getBytes(StandardCharsets.UTF_8));
	}

	public static String md5(InputStream input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] buf = new byte[1024 * 5];
			int readCount = 0;
			while ((readCount = input.read(buf)) > 0) {
				md.update(buf, 0, readCount);
			}
			return new String(md.digest());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static String md5(byte[] b) {
		return digest(b, "MD5");
	}

	public static String sha1(String s) {
		return sha1(s.getBytes(StandardCharsets.UTF_8));
	}

	public static String sha1(byte[] b) {
		return digest(b, "SHA1");
	}

	private static String digest(byte[] b, String algorithm) {
		try {
			// 获得摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance(algorithm);
			// 使用指定的字节更新摘要
			mdInst.update(b);
			// 获得密文
			byte[] data = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int l = data.length;
			char[] out = new char[l << 1];
			for (int i = 0, j = 0; i < l; i++) {
				out[j++] = HEX_CHARS[(0xF0 & data[i]) >>> 4];
				out[j++] = HEX_CHARS[0x0F & data[i]];
			}
			return new String(out);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

}
