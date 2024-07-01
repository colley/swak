/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.x500;

/**
 * The predefined X.500 attribute type keywords for the classes
 * {@link X500PrincipalBuilder} and
 * {@link javax.security.auth.x500.X500Principal}.
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2253.txt">RFC 2253</a>
 * @see <a href="http://www.ietf.org/rfc/rfc2459.txt">RFC 2459</a>
 */
public enum X500AttributeTypeKeyword {
    C, CN, DC, DNQ, DNQUALIFIER, EMAILADDRESS, GENERATION, GIVENNAME, INITIALS,
    L, O, OU, SERIALNUMBER, ST, STREET, SURNAME, T, UID
}
