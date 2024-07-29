/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 19-9-11 下午4:32
 * History:
 */
package com.swak.common.util;

import org.apache.commons.codec.net.URLCodec;

import java.nio.charset.Charset;


/**
 * 
 * ClassName: URLCoder.java 
 * @author colley.ma
 * @since 2.4.0
 */
public class URLCoder {
    public static String decode(final String str) {
        try {
            return new URLCodec().decode(str);
        } catch (Exception e) {
            // ignore
        }

        return str;
    }

    public static String decode(final String str, Charset charset) {
        try {
            return new URLCodec().decode(str, charset.toString());
        } catch (Exception e) {
            // ignore
        }

        return str;
    }

    public static String encode(final String str, Charset charset) {
        try {
            return new URLCodec().encode(str, charset.toString());
        } catch (Exception e) {
            // ignore
        }

        return str;
    }

    public static String encode(final String str) {
        try {
            return new URLCodec().encode(str);
        } catch (Exception e) {
            // ignore
        }

        return str;
    }
}
