/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api;

/** Provides encoders and decoders for generic object graphs. */
public interface Codec extends com.swak.license.api.io.Codec {

    /**
     * Returns an identifier for the content type used by this codec.
     * The returned string must conform to the syntax specified in
     * <a href="http://tools.ietf.org/html/rfc2045">RFC2045</a>
     * "Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet Message Bodies",
     * <a href="http://tools.ietf.org/html/rfc2045#section-5.1">Section 5.1</a>
     * "Syntax of the Content-Type Header Field",
     * except that it must not start with "Content-Type:"
     * and optional spaces, i.e. the field name must be stripped.
     */
    String contentType();

    /**
     * Returns an identifier for the content transfer encoding used by this
     * codec.
     * The returned string must conform to the syntax specified in
     * <a href="http://tools.ietf.org/html/rfc2045">RFC2045</a>
     * "Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet Message Bodies",
     * <a href="http://tools.ietf.org/html/rfc2045#section-6.1">Section 6.1</a>
     * "Content-Transfer-Encoding Syntax",
     * except that it must not start with "Content-Transfer-Encoding:"
     * and optional spaces, i.e. the field name must be stripped.
     * <p>
     * If the Content-Transfer-Encoding equals (ignoring case) {@code "8bit"}
     * and the  {@linkplain #contentType Content-Type} does not specify a
     * {@code charset} parameter, then {@code UTF-8} is assumed as the charset.
     */
    String contentTransferEncoding();
}
