/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.x500;


import com.swak.license.api.builder.GenBuilder;

import javax.security.auth.x500.X500Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * A generic builder for an {@link X500Principal}.
 * <p>
 * This abstract class is provided so that sub-classes can easily add methods
 * which return {@code this} without the need to cast its type by clients.
 * Example:
 * <pre>
 * {@code
 * class MyBuilder extends GenX500PrincipalBuilder<MyBuilder> {
 *     MyBuilder addFOO(String value) {
 *         addKeyword("FOO", "1.2.3.4.5.6.7.8.9");
 *         return addAttribute("FOO", value);
 *     }
 * }
 *
 * class MyClient {
 *     X500Principal create() {
 *         return new MyBuilder()
 *                 .addAttribute("CN", "Duke")
 *                 .addFOO("a")
 *                 .build();
 *     }
 * }
 * }</pre>
 * <p>
 * Notice that the call to {@code addFOO} is compilable because the previous
 * call to {@code addAttribute} is defined to return the generic type parameter
 * {@code This}.
 *
 * @param <This> the type of this generic X.500 principal builder.
 */
public abstract class GenX500PrincipalBuilder<This extends GenX500PrincipalBuilder<This>>
implements GenBuilder<X500Principal> {

    private final Map<String, String>
            attributes = new LinkedHashMap<>(),
            keywords = new HashMap<>();

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#C C}, value)</code>.
     */
    public This addC(String value) { return addAttribute(X500AttributeTypeKeyword.C, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#CN CN}, value)</code>.
     */
    public This addCN(String value) { return addAttribute(X500AttributeTypeKeyword.CN, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#DC DC}, value)</code>.
     */
    public This addDC(String value) { return addAttribute(X500AttributeTypeKeyword.DC, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#DNQ DNQ}, value)</code>.
     */
    public This addDNQ(String value) { return addAttribute(X500AttributeTypeKeyword.DNQ, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#DNQUALIFIER DNQUALIFIER}, value)</code>.
     */
    public This addDNQUALIFIER(String value) {
        return addAttribute(X500AttributeTypeKeyword.DNQUALIFIER, value);
    }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#EMAILADDRESS EMAILADDRESS}, value)</code>.
     */
    public This addEMAILADDRESS(String value) {
        return addAttribute(X500AttributeTypeKeyword.EMAILADDRESS, value);
    }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#GENERATION GENERATION}, value)</code>.
     */
    public This addGENERATION(String value) {
        return addAttribute(X500AttributeTypeKeyword.GENERATION, value);
    }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#GIVENNAME GIVENNAME}, value)</code>.
     */
    public This addGIVENNAME(String value) {
        return addAttribute(X500AttributeTypeKeyword.GIVENNAME, value);
    }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#INITIALS INITIALS}, value)</code>.
     */
    public This addINITIALS(String value) {
        return addAttribute(X500AttributeTypeKeyword.INITIALS, value);
    }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#L L}, value)</code>.
     */
    public This addL(String value) { return addAttribute(X500AttributeTypeKeyword.L, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#O O}, value)</code>.
     */
    public This addO(String value) { return addAttribute(X500AttributeTypeKeyword.O, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#OU OU}, value)</code>.
     */
    public This addOU(String value) { return addAttribute(X500AttributeTypeKeyword.OU, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#SERIALNUMBER SERIALNUMBER}, value)</code>.
     */
    public This addSERIALNUMBER(String value) {
        return addAttribute(X500AttributeTypeKeyword.SERIALNUMBER, value);
    }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#ST ST}, value)</code>.
     */
    public This addST(String value) { return addAttribute(X500AttributeTypeKeyword.ST, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#STREET STREET}, value)</code>.
     */
    public This addSTREET(String value) { return addAttribute(X500AttributeTypeKeyword.STREET, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#SURNAME SURNAME}, value)</code>.
     */
    public This addSURNAME(String value) {
        return addAttribute(X500AttributeTypeKeyword.SURNAME, value);
    }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#T T}, value)</code>.
     */
    public This addT(String value) { return addAttribute(X500AttributeTypeKeyword.T, value); }

    /**
     * Equivalent to
     * <code>addAttribute({@link X500AttributeTypeKeyword#UID UID}, value)</code>.
     */
    public This addUID(String value) { return addAttribute(X500AttributeTypeKeyword.UID, value); }

    /**
     * Adds an X.500 attribute to the distinguished name.
     *
     * @param keyword the attribute type keyword.
     * @param value the nullable, unquoted and unescaped attribute value.
     * @return {@code this}
     */
    public This addAttribute(X500AttributeTypeKeyword keyword, String value) {
        return addAttribute(keyword.name(), value);
    }

    /**
     * Adds an X.500 attribute to the distinguished name.
     *
     * @param type the attribute type,
     *             which can be referenced by a keyword or its OID.
     * @param value the nullable, unquoted and unescaped attribute value.
     * @return {@code this}
     * @see #addKeyword
     */
    @SuppressWarnings("unchecked")
    public This addAttribute(final String type, final String value) {
        requireNonNull(type);
        if (null != value) {
            attributes.put(type, value);
        } else {
            attributes.remove(type);
        }
        return (This) this;
    }

    /**
     * Adds a mapping from a keyword to an Object Identifier (OID).
     *
     * @param keyword the attribute type keyword.
     * @param oid the nullable OID.
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public This addKeyword(final String keyword, final String oid) {
        requireNonNull(keyword);
        if (null != oid) {
            keywords.put(keyword, oid);
        } else {
            keywords.remove(keyword);
        }
        return (This) this;
    }

    /**
     * Returns the X.500 principal with the distinguished name as built from
     * the sequence of attribute type and value pairs.
     *
     * @throws IllegalArgumentException if an attribute type is unknown or its
     *         keyword mapping is invalid.
     */
    @Override
    public X500Principal build() {
        final DistinguishedNameAssembler dna = new DistinguishedNameAssembler();
        for (Map.Entry<String, String> e : attributes.entrySet()) {
            dna.appendAttribute(e.getKey(), e.getValue());
        }
        return new X500Principal(dna.toString(), keywords);
    }

    private static final class DistinguishedNameAssembler {

        private static final String QUOTED_CHARACTERS_SEQUENCE = ",+<>;";

        private static final Pattern QUOTED_CHARACTERS_PATTERN =
                Pattern.compile(".*[" + QUOTED_CHARACTERS_SEQUENCE + "].*");

        private final StringBuilder dname = new StringBuilder();

        void appendAttribute(final String type, final String value) {
            // See http://www.ietf.org/rfc/rfc2253.txt
            delimitAttribute();
            dname.append(type).append('=');
            final boolean nq = needsQuoting(value);
            if (nq) {
                dname.append('"');
            }
            dname.append(escapeBackslashAndQuote(value));
            if (nq) {
                dname.append('"');
            }
        }

        private void delimitAttribute() {
            if (0 < dname.length()) {
                dname.append(',');
            }
        }

        private static boolean needsQuoting(String value) {
            return !value.equals(value.trim()) ||
                    value.startsWith("#") ||
                    QUOTED_CHARACTERS_PATTERN.matcher(value).matches();
        }

        private static String escapeBackslashAndQuote(String value) {
            return value.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        @Override public String toString() { return dname.toString(); }
    }
}
