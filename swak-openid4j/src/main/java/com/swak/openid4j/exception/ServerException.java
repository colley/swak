

package com.swak.openid4j.exception;


public class ServerException extends OpenIDException {
    public ServerException(String message) {
        super(message, SERVER_ERROR);
    }

    public ServerException(String message, int code) {
        super(message, code);
    }

    public ServerException(String message, Throwable cause) {
        super(message, SERVER_ERROR, cause);
    }

    public ServerException(String message, int code, Throwable cause) {
        super(message, code, cause);
    }

    public ServerException(Throwable cause) {
        super(cause);
    }

    public ServerException(int code, Throwable cause) {
        super(code, cause);
    }
}
