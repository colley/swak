package com.swak.core.security;

import lombok.Data;

import java.util.Set;

/**
 * DefaultTokenJwtDetails.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Data
public class DefaultTokenJwtDetails implements TokenJwtDetails {
    /**
     * 扩展字段：用户ID
     */
    private Long userId;
    /**
     * 手机验证码
     */
    private String mobileCode;
    /**
     * 权限列表
     */
    private Set<String> permissions;
    /**
     * 过期时间
     */
    private Long expireTime;
    private Long loginTime;
    private String token;
    /**
     * 默认字段
     */
    private String username;
    private String email;
}
