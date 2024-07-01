package com.swak.security.authentication;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.swak.common.dto.Response;
import com.swak.common.dto.ResponseResult;
import com.swak.core.security.SwakUserDetails;
import com.swak.security.dto.SwakUserDetailsImpl;
import com.swak.security.dto.UserAuthInfo;
import com.swak.security.service.SecurityAuthClientService;
import com.swak.security.service.UserTokenStore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DefaultUserTokenStore implements UserTokenStore {

    private SecurityAuthClientService securityAuthClientService;

    private final Cache<Long,SwakUserDetails> USER_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(500).build();

    public DefaultUserTokenStore(SecurityAuthClientService securityAuthClientService) {
        this.securityAuthClientService = securityAuthClientService;
    }

    @Override
    public Response<Void> store(SwakUserDetails userDetails) {
        USER_CACHE.put(userDetails.getUserId(),userDetails);
        return Response.success();
    }

    @Override
    public SwakUserDetails take(Long userId) {
        SwakUserDetails userDetails = USER_CACHE.getIfPresent(userId);
        if(Objects.nonNull(userDetails)){
            return userDetails;
        }
        Response<UserAuthInfo> response = securityAuthClientService.loadUserById(userId);
        UserAuthInfo userAuthInfo = response.getData();
        if(Objects.nonNull(userAuthInfo)) {
            userDetails = new SwakUserDetailsImpl(userAuthInfo,securityAuthClientService.getPermission(userId));
            USER_CACHE.put(userDetails.getUserId(),userDetails);
        }
        return userDetails;
    }

    public void setSecurityAuthClientService(SecurityAuthClientService securityAuthClientService) {
        this.securityAuthClientService = securityAuthClientService;
    }
}
