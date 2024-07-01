package com.swak.security.authentication;

import com.swak.core.security.SwakUserDetails;
import com.swak.security.config.JwtTokenConfig;
import com.swak.security.dto.JwtToken;
import com.swak.security.dto.LoginExtInfo;
import com.swak.security.service.SecurityAuthClientService;
import com.swak.security.service.UserTokenStore;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

public interface UserTokenService {

    SwakUserDetails getUserDetails(String  token);

     String getToken(HttpServletRequest request);

    JwtToken refreshToken(SwakUserDetails userDetails);

    JwtToken verifyToken(SwakUserDetails userDetails);

    LoginExtInfo getLoginExtInfo(HttpServletRequest request);

     JwtToken createToken(SwakUserDetails userDetails);

     JwtTokenConfig getJwtTokenConfig();

    UserTokenStore getUserDetailsStore();

    SecurityAuthClientService getAuthClientService();

    Set<String> getAuthWhitelist();

    Set<String> getStaticWhitelist();
}
