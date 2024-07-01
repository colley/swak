package com.swak.openid4j.message;

import com.swak.openid4j.exception.OpenIDException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class AuthFailure extends Message {
    private static final boolean DEBUG = log.isDebugEnabled();

    protected final static List requiredFields = Arrays.asList(new String[]{
            "openid.mode"
    });

    protected final static List optionalFields = Arrays.asList(new String[]{
            "openid.ns"
    });

    public AuthFailure(boolean compatibility, String returnTo) {
        set("openid.mode", MODE_CANCEL);

        if (!compatibility)
            set("openid.ns", OPENID2_NS);

        _destinationUrl = returnTo;
    }

    protected AuthFailure(ParameterList params) {
        super(params);
    }

    public static AuthFailure createAuthFailure(ParameterList params) throws
            MessageException {
        AuthFailure fail = new AuthFailure(params);

        fail.validate();

        if (DEBUG)
            log.debug("Retrieved auth failure from message parameters:\n"
                    + fail.keyValueFormEncoding());

        return fail;
    }

    public List getRequiredFields() {
        return requiredFields;
    }

    public boolean isVersion2() {
        return hasParameter("openid.ns") &&
                OPENID2_NS.equals(getParameterValue("openid.ns"));
    }

    public void validate() throws MessageException {
        super.validate();

        String mode = getParameterValue("openid.mode");

        if (!MODE_CANCEL.equals(mode))
            throw new MessageException(
                    "Invalid openid.mode; expected " +
                            MODE_CANCEL + " found: " + mode,
                    OpenIDException.AUTH_ERROR);
    }
}
