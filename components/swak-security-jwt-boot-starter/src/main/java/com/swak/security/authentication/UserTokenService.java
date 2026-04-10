package com.swak.security.authentication;

import com.swak.core.security.TokenJwtDetails;
import com.swak.security.config.JwtTokenConfig;
import com.swak.security.dto.JwtToken;
import com.swak.security.dto.LoginExtInfo;
import com.swak.security.service.SecurityAuthClientService;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

public interface UserTokenService {

    TokenJwtDetails getUserDetails(String  token);

     String getToken(HttpServletRequest request);

    JwtToken refreshToken(TokenJwtDetails userDetails);

    JwtToken verifyToken(TokenJwtDetails userDetails);

    LoginExtInfo getLoginExtInfo(HttpServletRequest request);

    String createTokenJwt(TokenJwtDetails userDetails);

     JwtTokenConfig getJwtTokenConfig();

    SecurityAuthClientService getAuthClientService();

    Set<String> getAuthWhitelist();

    Set<String> getStaticWhitelist();
}
