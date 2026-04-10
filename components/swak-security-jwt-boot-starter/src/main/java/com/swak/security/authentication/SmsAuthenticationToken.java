package com.swak.security.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SmsAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;

    private String smsCode;

    public SmsAuthenticationToken(String mobile,String smsCode) {
        super(null);
        this.principal = mobile;
        this.smsCode = smsCode;
        setAuthenticated(false);
    }

    public SmsAuthenticationToken(Object principal,String smsCode, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.smsCode = smsCode;
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public Object getCredentials() {
        return smsCode;
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "[Swak-Security] Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}