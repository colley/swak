package com.swak.security.authentication;

import com.swak.common.dto.Response;
import com.swak.core.security.SwakUserDetails;
import com.swak.security.enums.TokenResultCode;
import com.swak.security.exception.UserAccountException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final SwakUserDetailsService swakUserDetailsService;

    public SmsCodeAuthenticationProvider(SwakUserDetailsService swakUserDetailsService) {
        this.swakUserDetailsService = swakUserDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken smsAuthenticationToken = (SmsAuthenticationToken) authentication;
        String mobile = (String) smsAuthenticationToken.getPrincipal();
        String smsCode = (String) smsAuthenticationToken.getCredentials();
        SwakUserDetails userDetails = swakUserDetailsService.loadUserByMobile(mobile);
        if (Objects.isNull(userDetails)) {
            throw new UserAccountException(TokenResultCode.USER_NOT_FOUND);
        }
        Response<Void> matchSmsCode = swakUserDetailsService.verifySmsCode(mobile, smsCode);
        if(!matchSmsCode.isSuccess()) {
            throw new UserAccountException(matchSmsCode.getCode(),matchSmsCode.getMsg());
        }
        //获取权限信息
        userDetails.setPermissions(swakUserDetailsService.getPermission(userDetails.getUserId()));
       return createSuccessAuthentication(authentication, userDetails);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return SmsAuthenticationToken.class.isAssignableFrom(aClass);
    }

    /**
     * 认证成功将非授信凭据转为授信凭据.
     * 封装用户信息 角色信息。
     *
     * @param authentication the authentication
     * @param user           the user
     * @return the authentication
     */
    protected Authentication createSuccessAuthentication(Authentication authentication, UserDetails user) {
        Collection<? extends GrantedAuthority> authorities = authoritiesMapper.mapAuthorities(user.getAuthorities());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
        authenticationToken.setDetails(authentication.getDetails());
        return authenticationToken;
    }
}
