package com.swak.core.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

/**
 * 用户信息
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/13 10:27
 **/
public interface SwakUserDetails extends UserDetails {

    Long getUserId();

    Set<String> getPermissions();

    void setPermissions(Set<String> permissions);

    Long getExpireTime();

    String getToken();

    Long getLoginTime();

    void setExpireTime(Long expireTime);

    void setUserId(Long userId);

    void setUsername(String username);

    void setToken(String token);

    void setLoginTime(Long issuedAt);
}
