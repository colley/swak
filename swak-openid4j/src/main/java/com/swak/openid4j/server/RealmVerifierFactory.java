
package com.swak.openid4j.server;

import com.google.inject.Inject;
import com.swak.openid4j.discovery.yadis.YadisResolver;

/**
 * Factory object that, given a Yadis resolver, makes {@link RealmVerifier}s.
 */
public class RealmVerifierFactory {

    private final YadisResolver _yadisResolver;

    @Inject
    public RealmVerifierFactory(YadisResolver yadisResolver) {
        _yadisResolver = yadisResolver;
    }

    public RealmVerifier getRealmVerifierForConsumer() {
        return new RealmVerifier(false, _yadisResolver);
    }

    public RealmVerifier getRealmVerifierForServer() {
        return new RealmVerifier(true, _yadisResolver);
    }
}
