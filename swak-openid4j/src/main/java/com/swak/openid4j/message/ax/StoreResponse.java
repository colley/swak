

package com.swak.openid4j.message.ax;

import com.swak.openid4j.message.MessageException;
import com.swak.openid4j.message.Parameter;
import com.swak.openid4j.message.ParameterList;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;


@Slf4j
public class StoreResponse extends AxMessage {
    private static final boolean DEBUG = log.isDebugEnabled();

    /**
     * Constructs a Store Response with an empty parameter list.
     */
    protected StoreResponse() {
        _parameters.set(new Parameter("mode", "store_response_success"));

        if (DEBUG) log.debug("Created empty store request.");
    }

    /**
     * Constructs a Store Response with an empty parameter list.
     */
    public static StoreResponse createStoreResponse() {
        return new StoreResponse();
    }

    /**
     * Constructs a StoreResponse from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected StoreResponse(ParameterList params) {
        super(params);
    }

    /**
     * Constructs a StoreResponse from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    public static StoreResponse createStoreResponse(ParameterList params)
            throws MessageException {
        StoreResponse resp = new StoreResponse(params);

        if (!resp.isValid())
            throw new MessageException("Invalid parameters for a store response");

        if (DEBUG)
            log.debug("Created store response from parameter list:\n" + params);

        return resp;
    }

    /**
     * Marks the Store Response as a failure, by setting the appropirate
     * parameters.
     *
     * @param description Describes the error condition leading to
     *                    the failure response
     */
    public void setFailure(String description) {
        _parameters.set(new Parameter("mode", "store_response_failure"));

        if (description != null)
            _parameters.set(new Parameter("error", description));
    }

    /**
     * Returns true if the Store Response is a failure message, true if it is
     * a success response.
     */
    public boolean hasFailed() {
        return "store_response_failure".equals(
                _parameters.getParameterValue("mode"));
    }

    /**
     * Gets the status of the Store Response if the 'status' parameter is part
     * of the response, or null otherwise.
     */
    public String getErrorDescription() {
        return _parameters.getParameterValue("error");
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @return True if the extension is valid, false otherwise.
     */
    private boolean isValid() {
        if (!_parameters.hasParameter("mode") ||
                (!"store_response_success".equals(_parameters.getParameterValue("mode")) &&
                        !"store_response_failure".equals(_parameters.getParameterValue("mode")))) {
            log.warn("Invalid mode value in store response: "
                    + _parameters.getParameterValue("mode"));
            return false;
        }

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext()) {
            Parameter param = (Parameter) it.next();
            String paramName = param.getKey();

            if (!paramName.equals("mode") &&
                    !paramName.equals("error")) {
                log.warn("Invalid parameter name in store response: " + paramName);
                return false;
            }
        }

        return true;
    }

}
