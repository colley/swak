package com.swak.openid4j.message.sreg;

import com.swak.openid4j.message.MessageException;
import com.swak.openid4j.message.MessageExtension;
import com.swak.openid4j.message.MessageExtensionFactory;
import com.swak.openid4j.message.ParameterList;


/**
 * Custom Extension Factory for SREG 1.1 messages. Creates SRegMessage
 * objects, but sets the type URI to http://openid.net/extensions/sreg/1.1
 * for SREG 1.1.
 */
public class SReg11ExtensionFactory implements MessageExtensionFactory {
    /**
     * Gets the Type URI that identifies the Simple Registration 1.1 extension.
     */
    public String getTypeUri() {
        return SRegMessage.OPENID_NS_SREG11;
    }

    /**
     * Instantiates the apropriate Simple Registration object
     * (request / response) for the supplied parameter list.
     * <p>
     * Similar to SRegMessage.getExtension(), but sets the SREG 1.1 type URI.
     *
     * @param parameterList The Simple Registration specific parameters
     *                      (without the openid.<ext_alias> prefix)
     *                      extracted from the openid message.
     * @param isRequest     Indicates whether the parameters were
     *                      extracted from an OpenID request (true),
     *                      or from an OpenID response.
     * @return MessageExtension implementation for
     * the supplied extension parameters.
     * @throws MessageException If a Simple Registration object could not be
     *                          instantiated from the supplied parameter list.
     */
    public MessageExtension getExtension(
            ParameterList parameterList, boolean isRequest)
            throws MessageException {
        SRegMessage sreg;

        if (parameterList.hasParameter("required") ||
                parameterList.hasParameter("optional"))

            sreg = SRegRequest.createSRegRequest(parameterList);

        else
            sreg = SRegResponse.createSRegResponse(parameterList);

        sreg.setTypeUri(SRegMessage.OPENID_NS_SREG11);

        return sreg;
    }
}
