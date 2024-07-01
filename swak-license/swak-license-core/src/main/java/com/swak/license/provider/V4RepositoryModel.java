/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.provider;

import lombok.Data;

/**
 * A repository model for use with V4 format license keys.
 */
@Data
final class V4RepositoryModel {
    protected String algorithm;
    protected  String artifact;
    protected  String signature;
}
