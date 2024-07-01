/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.api.auth;

import java.security.GeneralSecurityException;

/**
 * Indicates that the integrity of a repository has probably been compromised
 * because the public key did not match the private key when verifying its
 * encoded artifact.
 */
public class RepositoryIntegrityException extends GeneralSecurityException {

    private static final long serialVersionUID = 0L;
}
