package com.swak.security.authentication;

import com.swak.common.dto.Response;
import com.swak.common.exception.SwakAssert;
import com.swak.core.security.AuthenticationListener;
import com.swak.core.security.SecurityUtils;
import com.swak.core.security.SwakUserDetails;
import com.swak.core.support.SpringBeanFactory;
import com.swak.security.config.TokenResultCode;
import com.swak.security.dto.JwtToken;
import com.swak.security.exception.UserAccountException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class TokenLoginServiceImpl implements TokenLoginService {

    @Setter
    private  AuthenticationManager authenticationManager;

    private final UserTokenService userTokenService;

    private final List<AuthenticationListener> authenticationListeners = new ArrayList<>();

    public TokenLoginServiceImpl(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
    }


    public AuthenticationManager getAuthenticationManager() {
        if(Objects.isNull(authenticationManager)) {
            this.authenticationManager = SpringBeanFactory.getBean(AuthenticationManager.class);
        }
        return authenticationManager;
    }



    @Override
    public Response<JwtToken> login(Authentication authenticationToken) {
        try {
            SwakAssert.notNull(authenticationToken, "authenticationToken cannot be null");
            SwakUserDetails userDetails;
            Authentication currentAuthentication = SecurityUtils.getAuthentication();
            if(Objects.nonNull(currentAuthentication) && currentAuthentication instanceof
                    UsernamePasswordAuthenticationToken) {
                userDetails = (SwakUserDetails) currentAuthentication.getPrincipal();
            }else{
                Authentication authentication = getAuthenticationManager().authenticate(authenticationToken);
                userDetails = (SwakUserDetails) authentication.getPrincipal();
            }
            // 生成token
            JwtToken jwtToken = userTokenService.createToken(userDetails);
            return Response.success(jwtToken);
        } catch (Exception e) {
            log.error("login exception principal:" + authenticationToken.getPrincipal(), e);
            if (e instanceof BadCredentialsException) {
                return Response.fail(TokenResultCode.USER_PWD_INCORRECT);
            }
            if (e instanceof UserAccountException) {
                UserAccountException userAccountEx = (UserAccountException) e;
                return Response.fail(userAccountEx.getCode(), userAccountEx.getMsg());
            }
            return Response.fail(TokenResultCode.USER_NOT_FOUND);
        }
    }

    @Override
    public Response<JwtToken> login(String username, String password) {
        return login(new UsernamePasswordAuthenticationToken(username, password));
    }

    @Override
    public Response<JwtToken> smsLogin(String mobile, String smsCode) {
        return login(new SmsAuthenticationToken(mobile, smsCode));
    }
}
