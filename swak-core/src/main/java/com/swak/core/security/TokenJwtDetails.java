package com.swak.core.security;

import com.swak.common.dto.base.DTO;

import java.util.Set;

/**
 * TokenUserDetails.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface TokenJwtDetails extends DTO{

    Long getUserId();

    Set<String> getPermissions();

    void setPermissions(Set<String> permissions);

    Long getExpireTime();

    String getToken();

    Long getLoginTime();

    void setExpireTime(Long expireTime);

    void setUserId(Long userId);

    void setUsername(String username);

    String getUsername();

    void setToken(String token);

    void setLoginTime(Long issuedAt);

    String getEmail();

    void setEmail(String email);
}
