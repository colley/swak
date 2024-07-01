
package com.swak.license.api.auth;


import com.swak.license.api.Codec;

/**
 * A factory for repository models and their associated {@linkplain RepositoryController repository controllers}.
 *
 * @param <Model> the generic repository model.
 */
public interface RepositoryFactory<Model> {

    /** Returns a new repository model. */
    Model model();

    /**
     * Returns the base class of all models created by this factory.
     */
    Class<Model> modelClass();

    /**
     * Returns a new repository controller using the given codec and repository model.
     */
    RepositoryController controller(Codec codec, Model model);
}
