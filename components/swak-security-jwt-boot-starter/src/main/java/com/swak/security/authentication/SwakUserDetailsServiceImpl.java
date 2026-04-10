package com.swak.security.authentication;

import com.swak.common.dto.Response;
import com.swak.common.util.UUIDHexGenerator;
import com.swak.core.security.SwakUserDetails;
import com.swak.security.config.JwtConstants;
import com.swak.security.dto.SwakUserDetailsImpl;
import com.swak.security.dto.UserAuthInfo;
import com.swak.security.exception.UserAccountException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

public class SwakUserDetailsServiceImpl implements SwakUserDetailsService {

    private final UserTokenService userTokenService;

    public SwakUserDetailsServiceImpl(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
    }

    @Override
    public SwakUserDetails loadUserByMobile(String mobile) throws UsernameNotFoundException {
        Response<UserAuthInfo> response = userTokenService.getAuthClientService().loadUserByMobile(mobile);
        if (!response.isSuccess()) {
            throw new UserAccountException(response.getCode(), response.getMsg());
        }
        UserAuthInfo userAuthInfo = response.getData();
        SwakUserDetailsImpl swakUserDetails = new SwakUserDetailsImpl(userAuthInfo, getPermission(userAuthInfo.getUserId()));
        userDetailsDecorate(swakUserDetails);
        return swakUserDetails;
    }

    private void userDetailsDecorate(SwakUserDetailsImpl swakUserDetails) {
        String token = UUIDHexGenerator.generator();
        Long expiresIn = userTokenService.getJwtTokenConfig().getToken().getExpireSeconds();
        long expireMillis = expiresIn * JwtConstants.MILLIS_SECOND;
        swakUserDetails.setExpireTime(System.currentTimeMillis() + expireMillis);
        swakUserDetails.setLoginTime(System.currentTimeMillis());
        swakUserDetails.setToken(token);
    }


    @Override
    public Response<Void> verifySmsCode(String mobile, String smsCode) {
        return userTokenService.getAuthClientService().verifySmsCode(mobile, smsCode);
    }

    @Override
    public Set<String> getPermission(Long userId) {
        return userTokenService.getAuthClientService().getPermission(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Response<UserAuthInfo> response = userTokenService.getAuthClientService().loadUserByUsername(username);
        if (!response.isSuccess()) {
            throw new UserAccountException(response.getCode(), response.getMsg());
        }
        UserAuthInfo userAuthInfo = response.getData();
        SwakUserDetailsImpl swakUserDetails = new SwakUserDetailsImpl(userAuthInfo, this.getPermission(userAuthInfo.getUserId()));
        userDetailsDecorate(swakUserDetails);
        return swakUserDetails;
    }
}
