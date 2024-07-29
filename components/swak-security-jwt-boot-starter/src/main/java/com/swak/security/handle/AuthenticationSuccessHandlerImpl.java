package com.swak.security.handle;

import com.swak.common.dto.Response;
import com.swak.common.util.ResponseRender;
import com.swak.core.security.AuthenticationListener;
import com.swak.core.security.SwakUserDetails;
import com.swak.core.security.TokenJwtDetails;
import com.swak.security.authentication.UserTokenService;
import com.swak.security.converter.UserDetailsConverter;
import com.swak.security.dto.JwtToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final List<AuthenticationListener> authenticationListeners = new ArrayList<>();

    private final UserTokenService userTokenService;

    @Resource
    private UserDetailsConverter userDetailsConverter;

    public AuthenticationSuccessHandlerImpl(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
        this.authenticationListeners.add(new LoginSuccessAuthenticationListener(userTokenService));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SwakUserDetails userDetails = (SwakUserDetails) authentication.getPrincipal();
        TokenJwtDetails tokenJwtDetails = userDetailsConverter.toTokenJwtDetails(userDetails);
        JwtToken token = new JwtToken().setAccess_token(userDetails.getToken())
                        .setLoginTime(userDetails.getLoginTime())
                                .setExpires_in(userTokenService.getJwtTokenConfig().getToken().getExpireSeconds());
        authenticationListeners.forEach(listener -> listener.onLoginSuccess(request, response, tokenJwtDetails));
        ResponseRender.renderJson(Response.success(token),response);
    }

    public void setAuthenticationListeners(List<AuthenticationListener> authenticationListeners) {
        if (!CollectionUtils.isEmpty(authenticationListeners)) {
            this.authenticationListeners.addAll(authenticationListeners);
        }
    }
}
