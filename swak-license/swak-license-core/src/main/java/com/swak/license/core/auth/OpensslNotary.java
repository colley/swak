
package com.swak.license.core.auth;

import com.swak.license.api.auth.Authentication;
import com.swak.license.api.auth.AuthenticationParameters;
import com.swak.license.api.auth.RepositoryController;
import com.swak.license.api.i18n.Message;
import com.swak.license.api.io.Decoder;
import com.swak.license.api.io.Source;
import com.swak.license.api.passwd.PasswordProtection;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Objects;
import java.util.Optional;

/**
 * Signs or verifies a generic artifact using a private or public key in a keystore entry.
 * This class is immutable.
 */
public final class OpensslNotary implements Authentication {

    private static final String DEFAULT_ALGORITHM = "MD5withRSA";

    private final AuthenticationParameters parameters;

    public OpensslNotary(final AuthenticationParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
    }

    @Override
    public Decoder sign(RepositoryController controller, Object artifact) throws Exception {
        return new Cache().sign(controller, artifact);
    }

    @Override
    public Decoder verify(RepositoryController controller) throws Exception {
        return new Cache().verify(controller);
    }

    private AuthenticationParameters parameters() {
        return parameters;
    }

    private final class Cache {

        private PrivateKey privateKey;
        private PublicKey publicKey;

        Decoder sign(RepositoryController controller, Object artifact) throws Exception {
            final Signature engine = engine();
            final PrivateKey key = privateKey();
            engine.initSign(key);
            return controller.sign(engine, artifact);
        }

        Decoder verify(RepositoryController controller) throws Exception {
            final Signature engine = engine();
            final PublicKey key = publicKey();
            engine.initVerify(key);
            return controller.verify(engine);
        }

        Signature engine() throws Exception {
            return Signature.getInstance(algorithm());
        }

        String algorithm() throws Exception {
            final Optional<String> configuredAlgorithm = configuredAlgorithm();
            return configuredAlgorithm.isPresent() ? configuredAlgorithm.get() : defaultAlgorithm();
        }

        String defaultAlgorithm() throws Exception {
            return DEFAULT_ALGORITHM;
        }

        PrivateKey privateKey() throws Exception {
            if (null == this.privateKey) {
                this.privateKey = RSAEncrypt.loadPrivateKey(parameters.source().get().input());
            }
            return this.privateKey;
        }

        PublicKey publicKey() throws Exception {
            if (null == this.publicKey) {
                this.publicKey = RSAEncrypt.loadPublicKey(parameters.source().get().input());
            }
            return this.publicKey;
        }

        Message message(String key) {
            return Messages.message(key, alias());
        }

        String alias() {
            return parameters().alias();
        }

        PasswordProtection keyProtection() {
            return parameters().keyProtection();
        }

        Optional<String> configuredAlgorithm() {
            return parameters().algorithm();
        }

        Optional<Source> source() {
            return parameters().source();
        }

        PasswordProtection storeProtection() {
            return parameters().storeProtection();
        }

        String storeType() {
            return parameters().storeType();
        }
    }
}
