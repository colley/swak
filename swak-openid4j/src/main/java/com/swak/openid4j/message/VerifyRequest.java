
package com.swak.openid4j.message;

import com.swak.openid4j.exception.OpenIDException;
import lombok.extern.slf4j.Slf4j;


/**
 * VerifyRequest is a AuthSuccess with the openid.mode
 * switched to check_authentication.
 */
@Slf4j
public class VerifyRequest extends AuthSuccess {
    private static final boolean DEBUG = log.isDebugEnabled();

    public static final String MODE_CHKAUTH = "check_authentication";

    protected VerifyRequest(AuthSuccess authResp) {
        super(convertAuthSuccessParams(authResp));
    }

    private static ParameterList convertAuthSuccessParams(AuthSuccess authResp) {
        ParameterList params = new ParameterList(authResp.getParameterMap());

        params.set(new Parameter("openid.mode", MODE_CHKAUTH));

        return params;
    }

    protected VerifyRequest(ParameterList params) {
        super(params);
    }

    public static VerifyRequest createVerifyRequest(AuthSuccess authResp)
            throws MessageException {
        VerifyRequest req = new VerifyRequest(authResp);

        req.validate();

        if (DEBUG) log.debug("Created verification request " +
                "from a positive auth response:\n" + req.keyValueFormEncoding());

        return req;
    }

    public static VerifyRequest createVerifyRequest(ParameterList params)
            throws MessageException {
        VerifyRequest req = new VerifyRequest(params);

        req.validate();

        if (DEBUG) log.debug("Created verification request:\n"
                + req.keyValueFormEncoding());

        return req;
    }

    public String getHandle() {
        return getParameterValue("openid.assoc_handle");
    }

    public String getInvalidateHandle() {
        return getParameterValue("openid.invalidate_handle");
    }

    public void validate() throws MessageException {
        if (!MODE_CHKAUTH.equals(getParameterValue("openid.mode"))) {
            throw new MessageException(
                    "Invalid openid.mode in verification request: "
                            + getParameterValue("openid.mode"),
                    OpenIDException.VERIFY_ERROR);
        }

        set("openid.mode", MODE_IDRES);

        if (DEBUG) log.debug("Delegating verification request validity check " +
                "to auth response...");

        super.validate();

        set("openid.mode", MODE_CHKAUTH);
    }
}
