/*
 * Copyright (C) 2005 - 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.swak.license.provider;

import com.swak.license.api.Codec;
import com.swak.license.api.auth.RepositoryController;
import com.swak.license.api.auth.RepositoryFactory;


/**
 * A repository factory for use with V4 format license keys.
 */
final class V4RepositoryFactory implements RepositoryFactory<V4RepositoryModel> {

    @Override
    public V4RepositoryModel model() {
        return new V4RepositoryModel();
    }

    @Override
    public Class<V4RepositoryModel> modelClass() {
        return V4RepositoryModel.class;
    }

    @Override
    public RepositoryController controller(Codec codec, V4RepositoryModel model) {
        return new V4RepositoryController(codec, model);
    }
}
