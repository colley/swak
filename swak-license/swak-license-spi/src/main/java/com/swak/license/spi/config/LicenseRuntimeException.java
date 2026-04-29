package com.swak.license.spi.config;

import com.swak.license.api.LicenseValidationException;

/**
 * LicenseRuntimeException.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public class LicenseRuntimeException extends RuntimeException{

    public LicenseRuntimeException(LicenseValidationException exception){
        super(exception.getMessage());
    }

    public LicenseRuntimeException(Exception exception){
        super(exception);
    }
}
