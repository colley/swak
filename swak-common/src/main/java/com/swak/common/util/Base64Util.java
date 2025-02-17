package com.swak.common.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 * Base64Util
 * @author colley.ma
 * @since 2022/9/9 18:40
 */
public class Base64Util {

	public static String encode2Str(String data) {
		return encode2Str(data, null, false);
	}

	public static String encode2Str(String data, String encoding) {
		return encode2Str(data, encoding, false);
	}

	public static String encode2Str(byte[] data) {
		return encode2Str(data, null, false);
	}

	public static String encode2Str(byte[] data, String encoding) {
		return encode2Str(data, encoding, false);
	}

	public static byte[] encode(String data) {
		return encode(data, null, false);
	}

	public static byte[] encode(String data, String encoding) {
		return encode(data, encoding, false);
	}

	public static byte[] encode(byte[] data) {
		return encode(data, false);
	}

	public static String encode4url(String data) {
		return encode2Str(data, null, true);
	}

	public static String encode2Str(String data, String encoding,
                                    boolean urlSafe) {
		if (StringUtils.isBlank(data)) {
			return null;
		}
		if (StringUtils.isBlank(encoding)) {
			encoding = StringPool.UTF8;
		}
		byte[] result = encode(data, encoding, urlSafe);
		return org.apache.commons.codec.binary.StringUtils.newString(result,
				encoding);
	}

	public static byte[] encode(String data, String encoding, boolean urlSafe) {
		if (StringUtils.isBlank(data)) {
			return null;
		}
		if (StringUtils.isBlank(encoding)) {
			encoding = StringPool.UTF8;
		}
		return encode(
				org.apache.commons.codec.binary.StringUtils.getBytesUnchecked(
						data, encoding), urlSafe);
	}

	public static String encode2Str(byte[] data, String encoding,
                                    boolean urlSafe) {
		if (data == null) {
			return null;
		}
		if (StringUtils.isBlank(encoding)) {
			encoding = StringPool.UTF8;
		}
		byte[] result = encode(data, urlSafe);
		return org.apache.commons.codec.binary.StringUtils.newString(result,
				encoding);
	}

	public static byte[] encode(byte[] data, boolean urlSafe) {
		if (data == null) {
			return null;
		}
		Base64 base64 = new Base64(urlSafe);
		return base64.encode(data);
	}

	// ####################################### decode
	// ##############################

	public static String decode2Str(String data) {
		return decode2Str(data, null, false);
	}

	public static String decode2Str(String data, String encoding) {
		return decode2Str(data, encoding, false);
	}

	public static String decode2Str(byte[] data) {
		return decode2Str(data, null, false);
	}

	public static String decode2Str(byte[] data, String encoding) {
		return decode2Str(data, encoding, false);
	}

	public static byte[] decode(String data) {
		return decode(data, null, false);
	}

	public static byte[] decode(String data, String encoding) {
		return decode(data, encoding, false);
	}

	public static byte[] decode(byte[] data) {
		return decode(data, false);
	}

	public static String decode4url(String data) {
		return decode2Str(data, null, true);
	}

	public static String decode2Str(String data, String encoding,
                                    boolean urlSafe) {
		if (StringUtils.isBlank(data)) {
			return null;
		}
		if (StringUtils.isBlank(encoding)) {
			encoding = StringPool.UTF8;
		}
		return decode2Str(
				org.apache.commons.codec.binary.StringUtils.getBytesUnchecked(
						data, encoding), encoding, urlSafe);
	}

	public static String decode2Str(byte[] data, String encoding,
                                    boolean urlSafe) {
		if (data == null) {
			return null;
		}
		if (StringUtils.isBlank(encoding)) {
			encoding = StringPool.UTF8;
		}
		byte[] result = decode(data, urlSafe);
		return org.apache.commons.codec.binary.StringUtils.newString(result,
				encoding);
	}

	public static byte[] decode(String data, String encoding, boolean urlSafe) {
		if (StringUtils.isBlank(data)) {
			return null;
		}
		if (StringUtils.isBlank(encoding)) {
			encoding = StringPool.UTF8;
		}
		return decode(
				org.apache.commons.codec.binary.StringUtils.getBytesUnchecked(
						data, encoding), urlSafe);
	}

	public static byte[] decode(byte[] data, boolean urlSafe) {
		if (data == null) {
			return null;
		}
		Base64 base64 = new Base64(urlSafe);
		return base64.decode(data);
	}

}
