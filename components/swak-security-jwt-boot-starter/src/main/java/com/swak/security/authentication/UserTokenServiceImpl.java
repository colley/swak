package com.swak.security.authentication;

import com.google.common.collect.Sets;
import com.swak.common.exception.SwakAssert;
import com.swak.common.util.GetterUtil;
import com.swak.core.security.JwtTokenUtils;
import com.swak.core.security.TokenJwtDetails;
import com.swak.core.web.AddressUtils;
import com.swak.core.web.ServletUtils;
import com.swak.security.config.JwtConstants;
import com.swak.security.config.JwtTokenConfig;
import com.swak.security.config.WhitelistConfig;
import com.swak.security.dto.JwtToken;
import com.swak.security.dto.LoginExtInfo;
import com.swak.security.enums.TokenResultCode;
import com.swak.security.exception.JwtTokenException;
import com.swak.security.service.SecurityAuthClientService;
import com.swak.security.spi.TokenJwtExchange;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class UserTokenServiceImpl implements UserTokenService, InitializingBean {

    private JwtTokenConfig jwtTokenConfig;

    private SecurityAuthClientService securityAuthClientService;

    @Autowired(required = false)
    private WhitelistConfig whitelistConfig;

    @Override
    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(jwtTokenConfig.getToken().getHeader());
        if (StringUtils.isNotEmpty(token) && token.startsWith(JwtConstants.TOKEN_PREFIX)) {
            token = token.replace(JwtConstants.TOKEN_PREFIX, "");
        }
        return GetterUtil.getString(token);
    }

    public JwtToken refreshToken(TokenJwtDetails userDetails) {
        Long expiresIn = jwtTokenConfig.getToken().getExpireSeconds();
        long expireMillis = expiresIn * JwtConstants.MILLIS_SECOND;
        userDetails.setExpireTime(System.currentTimeMillis() + expireMillis);
        String tokenJwt = createTokenJwt(userDetails);
        TokenJwtExchange.getTokenJwtExchange().refresh(userDetails.getToken(), tokenJwt, expiresIn);
        return  new JwtToken()
                .setAccess_token(userDetails.getToken())
                .setLoginTime(userDetails.getLoginTime())
                .setExpires_in(expiresIn);
    }

    public JwtToken verifyToken(TokenJwtDetails loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime <= currentTime) {
            return refreshToken(loginUser);
        }
        return new JwtToken()
                .setAccess_token(loginUser.getToken())
                .setLoginTime(loginUser.getLoginTime())
                .setExpires_in(jwtTokenConfig.getToken().getExpireSeconds());
    }

    @Override
    public String createTokenJwt(TokenJwtDetails userDetails) {
        String secret = jwtTokenConfig.getToken().getSecret();
        return JwtTokenUtils.encode(userDetails, secret);
    }

    @Override
    public TokenJwtDetails getUserDetails(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try { // 获取请求携带的令牌
            String secret = jwtTokenConfig.getToken().getSecret();
            String userJwt = TokenJwtExchange.getTokenJwtExchange().takeTokenJwt(token);
            if (StringUtils.isEmpty(userJwt)) {
                return null;
            }
            return JwtTokenUtils.decode(userJwt, secret);
        } catch (ExpiredJwtException e) {
            // 异常捕获、发送到ExpiredJwtException
            throw new JwtTokenException(TokenResultCode.TOKEN_EXPIRED, e);
        } catch (SignatureException e) {
            throw new JwtTokenException(TokenResultCode.TOKEN_SIGN, e);
        } catch (Exception e) {
            throw new JwtTokenException(TokenResultCode.TOKEN_ILLEGAL, e);
        }
    }


    @Override
    public JwtTokenConfig getJwtTokenConfig() {
        return jwtTokenConfig;
    }

    @Override
    public SecurityAuthClientService getAuthClientService() {
        return this.securityAuthClientService;
    }

    @Override
    public Set<String> getAuthWhitelist() {
        whitelistConfig = Optional.ofNullable(whitelistConfig).orElse(new WhitelistConfig());
        Set<String> authWhitelist = new HashSet<>();
        Optional.ofNullable(jwtTokenConfig.getPermitUrls()).orElse(Collections.emptyList())
                .stream().filter(StringUtils::isNotEmpty).map(StringUtils::trim)
                .forEach(authWhitelist::add);
        authWhitelist.add(jwtTokenConfig.getLoginUrl());
        authWhitelist.addAll(whitelistConfig.getAuthWhitelist());
        authWhitelist.addAll(Sets.newHashSet(JwtConstants.AuthWhiteList.AUTH_WHITELIST));
        return authWhitelist;
    }

    @Override
    public Set<String> getStaticWhitelist() {
        whitelistConfig = Optional.ofNullable(whitelistConfig).orElse(new WhitelistConfig());
        Set<String> staticWhitelist = new HashSet<>(Sets.newHashSet(JwtConstants.AuthWhiteList.AUTH_WHITELIST));
        staticWhitelist.addAll(whitelistConfig.getStaticWhitelist());
        return staticWhitelist;
    }

    @Override
    public LoginExtInfo getLoginExtInfo(HttpServletRequest request) {
        LoginExtInfo extInfo = new LoginExtInfo();
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        String ip = ServletUtils.getIpAddr();
        extInfo.setIpaddr(ip);
        extInfo.setLoginLocation(AddressUtils.getRealAddressByIP(ip, jwtTokenConfig.getAddressEnabled()));
        extInfo.setBrowser(userAgent.getBrowser().getName());
        extInfo.setOs(userAgent.getOperatingSystem().getName());
        return extInfo;
    }


    public void setJwtTokenConfig(JwtTokenConfig jwtTokenConfig) {
        this.jwtTokenConfig = jwtTokenConfig;
    }


    public void setSecurityAuthClientService(SecurityAuthClientService securityAuthClientService) {
        this.securityAuthClientService = securityAuthClientService;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        SwakAssert.notNull(securityAuthClientService, "[Swak-Security] securityAuthClientService cannot be null");
        SwakAssert.notNull(jwtTokenConfig, "[Swak-Security] jwtTokenConfig cannot be null");
    }
}
