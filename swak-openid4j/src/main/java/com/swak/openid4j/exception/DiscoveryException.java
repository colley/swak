

package com.swak.openid4j.exception;


public class DiscoveryException extends OpenIDException {
    public DiscoveryException(String message) {
        super(message, DISCOVERY_ERROR);
    }

    public DiscoveryException(String message, int code) {
        super(message, code);
    }

    public DiscoveryException(Throwable cause) {
        super(DISCOVERY_ERROR, cause);
    }

    public DiscoveryException(int code, Throwable cause) {
        super(code, cause);
    }

    public DiscoveryException(String message, Throwable cause) {
        super(message, DISCOVERY_ERROR, cause);
    }

    public DiscoveryException(String message, int code, Throwable cause) {
        super(message, code, cause);
    }
}
