

package com.swak.openid4j.server;

import com.swak.openid4j.exception.ServerException;
import com.swak.openid4j.message.*;
import com.swak.openid4j.message.ax.AxMessage;
import com.swak.openid4j.message.ax.FetchRequest;
import com.swak.openid4j.message.ax.FetchResponse;
import com.swak.openid4j.message.sreg.SRegMessage;
import com.swak.openid4j.message.sreg.SRegRequest;
import com.swak.openid4j.message.sreg.SRegResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample Server (OpenID Provider) implementation.
 */
public class SampleServer {
    // instantiate a ServerManager object
    public ServerManager manager = new ServerManager();

    public SampleServer() {
        this("http://my.openidprovider.com/server");
    }

    public SampleServer(String endPointUrl) {
        manager.setOPEndpointUrl(endPointUrl);
        // for a working demo, not enforcing RP realm discovery
        // since this new feature is not deployed
        manager.getRealmVerifier().setEnforceRpId(false);
    }

    public String processRequest(HttpServletRequest httpReq,
                                 HttpServletResponse httpResp)
            throws Exception {
        // extract the parameters from the request
        ParameterList request = new ParameterList(httpReq.getParameterMap());

        String mode = request.hasParameter("openid.mode") ?
                request.getParameterValue("openid.mode") : null;

        Message response;
        String responseText;

        if ("associate".equals(mode)) {
            // --- process an association request ---
            response = manager.associationResponse(request);
            responseText = response.keyValueFormEncoding();
        } else if ("checkid_setup".equals(mode)
                || "checkid_immediate".equals(mode)) {
            // interact with the user and obtain data needed to continue
            List userData = userInteraction(request);

            String userSelectedClaimedId = (String) userData.get(0);
            Boolean authenticatedAndApproved = (Boolean) userData.get(1);
            String email = (String) userData.get(2);

            // --- process an authentication request ---
            AuthRequest authReq =
                    AuthRequest.createAuthRequest(request, manager.getRealmVerifier());

            String opLocalId = null;
            // if the user chose a different claimed_id than the one in request
            if (userSelectedClaimedId != null &&
                    userSelectedClaimedId.equals(authReq.getClaimed())) {
                //opLocalId = lookupLocalId(userSelectedClaimedId);
            }

            response = manager.authResponse(request,
                    opLocalId,
                    userSelectedClaimedId,
                    authenticatedAndApproved.booleanValue(),
                    false); // Sign after we added extensions.

            if (response instanceof DirectError)
                return directResponse(httpResp, response.keyValueFormEncoding());
            else {
                if (authReq.hasExtension(AxMessage.OPENID_NS_AX)) {
                    MessageExtension ext = authReq.getExtension(AxMessage.OPENID_NS_AX);
                    if (ext instanceof FetchRequest) {
                        FetchRequest fetchReq = (FetchRequest) ext;
                        Map required = fetchReq.getAttributes(true);
                        //Map optional = fetchReq.getAttributes(false);
                        if (required.containsKey("email")) {
                            Map userDataExt = new HashMap();
                            //userDataExt.put("email", userData.get(3));

                            FetchResponse fetchResp =
                                    FetchResponse.createFetchResponse(fetchReq, userDataExt);
                            // (alternatively) manually add attribute values
                            fetchResp.addAttribute("email",
                                    "http://schema.openid.net/contact/email", email);
                            response.addExtension(fetchResp);
                        }
                    } else //if (ext instanceof StoreRequest)
                    {
                        throw new UnsupportedOperationException("TODO");
                    }
                }
                if (authReq.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                    MessageExtension ext = authReq.getExtension(SRegMessage.OPENID_NS_SREG);
                    if (ext instanceof SRegRequest) {
                        SRegRequest sregReq = (SRegRequest) ext;
                        List required = sregReq.getAttributes(true);
                        //List optional = sregReq.getAttributes(false);
                        if (required.contains("email")) {
                            // data released by the user
                            Map userDataSReg = new HashMap();
                            //userData.put("email", "user@example.com");

                            SRegResponse sregResp = SRegResponse.createSRegResponse(sregReq, userDataSReg);
                            // (alternatively) manually add attribute values
                            sregResp.addAttribute("email", email);
                            response.addExtension(sregResp);
                        }
                    } else {
                        throw new UnsupportedOperationException("TODO");
                    }
                }

                // Sign the auth success message.
                // This is required as AuthSuccess.buildSignedList has a `todo' tag now.
                manager.sign((AuthSuccess) response);

                // caller will need to decide which of the following to use:

                // option1: GET HTTP-redirect to the return_to URL
                return response.getDestinationUrl(true);

                // option2: HTML FORM Redirection
                //RequestDispatcher dispatcher =
                //        getServletContext().getRequestDispatcher("formredirection.jsp");
                //httpReq.setAttribute("prameterMap", response.getParameterMap());
                //httpReq.setAttribute("destinationUrl", response.getDestinationUrl(false));
                //dispatcher.forward(request, response);
                //return null;
            }
        } else if ("check_authentication".equals(mode)) {
            // --- processing a verification request ---
            response = manager.verify(request);
            responseText = response.keyValueFormEncoding();
        } else {
            // --- error response ---
            response = DirectError.createDirectError("Unknown request");
            responseText = response.keyValueFormEncoding();
        }

        // return the result to the user
        return responseText;
    }

    protected List userInteraction(ParameterList request) throws ServerException {
        throw new ServerException("User-interaction not implemented.");
    }

    private String directResponse(HttpServletResponse httpResp, String response)
            throws IOException {
        ServletOutputStream os = httpResp.getOutputStream();
        os.write(response.getBytes());
        os.close();

        return null;
    }
}
