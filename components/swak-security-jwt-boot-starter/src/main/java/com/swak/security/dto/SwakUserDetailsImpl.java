package com.swak.security.dto;

import com.swak.common.enums.Married;
import com.swak.core.security.SwakUserDetails;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Data
public  class SwakUserDetailsImpl implements SwakUserDetails {
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

    private String password;

    private Boolean enabled;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;


    public SwakUserDetailsImpl() {
    }

    public SwakUserDetailsImpl(UserAuthInfo userAuth, Set<String> permissions) {
        this(userAuth);
        this.permissions = permissions;
    }

    public SwakUserDetailsImpl(UserAuthInfo user) {
        this.setUserId(user.getUserId());
        this.setUsername(user.getUsername());
        this.setPassword(user.getPassword());
        this.setEnabled(Married.isTrue(user.getStatus()));
    }

    public SwakUserDetailsImpl(Long userId, String username, Set<String> permissions) {
        this(userId, username, null, permissions);
    }

    public SwakUserDetailsImpl(Long userId, String username, String password, Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.permissions = permissions;
        this.password = password;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账户是否未过期,过期无法验证
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * 指定用户是否解锁,锁定的用户无法进行身份验证
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * 指示是否已过期的用户的凭据(密码),过期的凭据防止认证
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * 是否可用 ,禁用的用户不能身份验证
     */
    @Override
    public boolean isEnabled() {
        return Optional.ofNullable(enabled).orElse(true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Optional.ofNullable(permissions).orElse(Collections.emptySet())
                .stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
