

package com.swak.openid4j.message.ax;

import com.swak.openid4j.message.MessageException;
import com.swak.openid4j.message.Parameter;
import com.swak.openid4j.message.ParameterList;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements the extension for Attribute Exchange store requests.
 */
@Slf4j
public class StoreRequest extends AxPayload {
    private static final boolean DEBUG = log.isDebugEnabled();

    /**
     * Constructs a Store Request with an empty parameter list.
     */
    protected StoreRequest() {
        _parameters.set(new Parameter("mode", "store_request"));

        if (DEBUG) log.debug("Created empty store request.");
    }

    /**
     * Constructs a Store Request with an empty parameter list.
     */
    public static StoreRequest createStoreRequest() {
        return new StoreRequest();
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected StoreRequest(ParameterList params) {
        _parameters = params;
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    public static StoreRequest createStoreRequest(ParameterList params)
            throws MessageException {
        StoreRequest req = new StoreRequest(params);

        if (!req.isValid())
            throw new MessageException("Invalid parameters for a store request");

        if (DEBUG)
            log.debug("Created store request from parameter list:\n" + params);

        return req;
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @return True if the extension is valid, false otherwise.
     */
    public boolean isValid() {
        if (!_parameters.hasParameter("mode") ||
                !"store_request".equals(_parameters.getParameterValue("mode"))) {
            log.warn("Invalid mode value in store_request: "
                    + _parameters.getParameterValue("mode"));
            return false;
        }

        return super.isValid();
    }

}
