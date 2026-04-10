package com.swak.security.handle;

import com.swak.core.security.AuthenticationListener;
import com.swak.core.security.TokenJwtDetails;
import com.swak.security.authentication.UserTokenService;
import com.swak.security.spi.TokenJwtExchange;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LoginSuccessAuthenticationListener.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class LoginSuccessAuthenticationListener implements AuthenticationListener {

    private final UserTokenService userTokenService;

    public LoginSuccessAuthenticationListener(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
    }

    @Override
    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, TokenJwtDetails userDetails) {
        String tokenJwt = userTokenService.createTokenJwt(userDetails);
        if (StringUtils.isNotBlank(tokenJwt)) {
            Long expireSeconds = (userDetails.getExpireTime() - userDetails.getLoginTime())/1000;
            TokenJwtExchange.getTokenJwtExchange().storeTokenJwt(userDetails.getToken(), tokenJwt,expireSeconds);
        }
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response) {
        String token = userTokenService.getToken(request);
        if (StringUtils.isNotBlank(token)) {
            TokenJwtExchange.getTokenJwtExchange().remove(token);
        }
    }
}
