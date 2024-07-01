package com.swak.security.handle;

import com.swak.common.dto.Response;
import com.swak.common.util.ResponseRender;
import com.swak.core.security.AuthenticationListener;
import com.swak.core.security.SwakUserDetails;
import com.swak.security.authentication.UserTokenService;
import com.swak.security.dto.JwtToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final List<AuthenticationListener> authenticationListeners = new ArrayList<>();

    private final UserTokenService userTokenService;

    public AuthenticationSuccessHandlerImpl(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SwakUserDetails userDetails = (SwakUserDetails) authentication.getPrincipal();
        authenticationListeners.forEach(listener -> listener.onLoginSuccess(request, response, userDetails));
        userTokenService.getUserDetailsStore().store(userDetails);
        JwtToken token = userTokenService.createToken(userDetails);
        ResponseRender.renderJson(Response.success(token),response);
    }

    public void setAuthenticationListeners(List<AuthenticationListener> authenticationListeners) {
        if (!CollectionUtils.isEmpty(authenticationListeners)) {
            this.authenticationListeners.addAll(authenticationListeners);
        }
    }
}
