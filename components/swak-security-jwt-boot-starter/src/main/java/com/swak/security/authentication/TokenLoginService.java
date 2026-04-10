package com.swak.security.authentication;

import com.swak.common.dto.Response;
import com.swak.security.dto.JwtToken;
import org.springframework.security.core.Authentication;

public interface TokenLoginService {
    /**
     * 登录
     */
    Response<JwtToken> login(Authentication authenticationToken);

    Response<JwtToken> login(String username, String password);

    Response<JwtToken> smsLogin(String mobile, String smsCode);
}