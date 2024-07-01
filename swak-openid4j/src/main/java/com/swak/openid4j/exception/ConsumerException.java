

package com.swak.openid4j.exception;


public class ConsumerException extends OpenIDException {

    public ConsumerException(String message) {
        super(message, CONSUMER_ERROR);
    }

    public ConsumerException(String message, int code) {
        super(message, code);
    }

    public ConsumerException(String message, Throwable cause) {
        super(message, CONSUMER_ERROR, cause);
    }

    public ConsumerException(String message, int code, Throwable cause) {
        super(message, code, cause);
    }

    public ConsumerException(Throwable cause) {
        super(SERVER_ERROR, cause);
    }

    public ConsumerException(int code, Throwable cause) {
        super(code, cause);
    }
}
