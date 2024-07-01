
package com.swak.openid4j.message;

import com.swak.openid4j.exception.OpenIDException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;


@Slf4j
public class VerifyResponse extends Message {
    private static final boolean DEBUG = log.isDebugEnabled();

    protected final static List requiredFields = Arrays.asList(new String[]{
            "is_valid"
    });

    protected final static List optionalFields = Arrays.asList(new String[]{
            "ns",
            "invalidate_handle"
    });

    protected VerifyResponse(boolean compatibility) {
        setSignatureVerified(false);

        if (!compatibility)
            set("ns", OPENID2_NS);
    }

    protected VerifyResponse(ParameterList params) {
        super(params);
    }

    public static VerifyResponse createVerifyResponse(boolean compatibility)
            throws MessageException {
        VerifyResponse resp = new VerifyResponse(compatibility);

        resp.validate();

        if (DEBUG) log.debug("Created verification response:\n"
                + resp.keyValueFormEncoding());

        return resp;
    }

    public static VerifyResponse createVerifyResponse(ParameterList params)
            throws MessageException {
        VerifyResponse resp = new VerifyResponse(params);

        resp.validate();

        if (DEBUG) log.debug("Created verification response:\n"
                + resp.keyValueFormEncoding());

        return resp;
    }

    public List getRequiredFields() {
        return requiredFields;
    }

    public boolean isVersion2() {
        return hasParameter("ns") && OPENID2_NS.equals(getParameterValue("ns"));
    }

    public void setSignatureVerified(boolean verified) {
        if (DEBUG)
            log.debug("Setting is_valid to: " + verified);

        set("is_valid", verified ? "true" : "false");
    }

    public boolean isSignatureVerified() {
        return "true".equals(getParameterValue("is_valid"));
    }

    public void setInvalidateHandle(String handle) {
        set("invalidate_handle", handle);
    }

    public String getInvalidateHandle() {
        return getParameterValue("invalidate_handle");
    }

    public void validate() throws MessageException {
        super.validate();

        if (!"true".equals(getParameterValue("is_valid")) &&
                !"false".equals(getParameterValue("is_valid"))) {
            throw new MessageException(
                    "Invalid is_valid value in verification response: "
                            + getParameterValue("is_valid"),
                    OpenIDException.VERIFY_ERROR);
        }
    }
}
