package com.swak.common.util;

import java.nio.charset.Charset;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public final class ByteUtils {
    public static final byte[] EMPTY = new byte[0];

    public ByteUtils() {
    }

    public static byte[] toBytes(String input) {
        return input == null ? EMPTY : input.getBytes(Charset.forName("UTF-8"));
    }

    public static byte[] toBytes(Object obj) {
        return obj == null ? EMPTY : toBytes(String.valueOf(obj));
    }

    public static String toString(byte[] bytes) {
        return bytes == null ? "" : new String(bytes, Charset.forName("UTF-8"));
    }

    public static boolean isEmpty(byte[] data) {
        return data == null || data.length == 0;
    }

    public static boolean isNotEmpty(byte[] data) {
        return !isEmpty(data);
    }
}
