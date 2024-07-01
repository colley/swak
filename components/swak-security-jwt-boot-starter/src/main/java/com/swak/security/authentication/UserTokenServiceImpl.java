package com.swak.security.authentication;

import com.google.common.collect.Sets;
import com.swak.common.exception.SwakAssert;
import com.swak.common.util.GetterUtil;
import com.swak.core.security.SwakUserDetails;
import com.swak.core.web.AddressUtils;
import com.swak.core.web.ServletUtils;
import com.swak.security.config.JwtConstants;
import com.swak.security.config.JwtTokenConfig;
import com.swak.security.config.WhitelistConfig;
import com.swak.security.dto.JwtToken;
import com.swak.security.dto.LoginExtInfo;
import com.swak.security.exception.JwtTokenException;
import com.swak.security.service.SecurityAuthClientService;
import com.swak.security.service.UserTokenStore;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
public class UserTokenServiceImpl implements UserTokenService, InitializingBean {

    private UserTokenStore userTokenStore;

    private JwtTokenConfig jwtTokenConfig;

    private SecurityAuthClientService securityAuthClientService;

    @Autowired(required = false)
    private WhitelistConfig whitelistConfig;

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtTokenConfig.getToken().getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(jwtTokenConfig.getToken().getHeader());
        if (StringUtils.isNotEmpty(token) && token.startsWith(JwtConstants.TOKEN_PREFIX)) {
            token = token.replace(JwtConstants.TOKEN_PREFIX, "");
        }
        return GetterUtil.getString(token);
    }

    public JwtToken refreshToken(SwakUserDetails loginUser) {
        return createToken(loginUser);
    }

    public JwtToken verifyToken(SwakUserDetails loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime <= currentTime) {
            return refreshToken(loginUser);
        }
        return new JwtToken()
                .setAccess_token(loginUser.getToken())
                .setLoginTime(loginUser.getLoginTime())
                .setExpires_in(jwtTokenConfig.getToken().getExpireTime());
    }

    @Override
    public JwtToken createToken(SwakUserDetails userDetails) {
        JwtToken jwtToken = new JwtToken();
        Long expiresIn = jwtTokenConfig.getToken().getExpireTime();
        long expireMillis = expiresIn * JwtConstants.MILLIS_SECOND;
        userDetails.setExpireTime(System.currentTimeMillis() + expireMillis);
        String secret = jwtTokenConfig.getToken().getSecret();
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.USER_NAME, userDetails.getUsername());
        claims.put(JwtConstants.USER_ID, userDetails.getUserId());
        String token = Jwts.builder()
                .setClaims(claims)
                .compressWith(CompressionCodecs.DEFLATE)
                .setIssuedAt(new Date(userDetails.getLoginTime()))
                .setExpiration(new Date(userDetails.getExpireTime()))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        userDetails.setToken(token);
        jwtToken.setAccess_token(token);
        jwtToken.setLoginTime(userDetails.getLoginTime());
        jwtToken.setExpires_in(expiresIn);
        return jwtToken;
    }


    @Override
    public SwakUserDetails getUserDetails(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try {   // 获取请求携带的令牌
            Claims claims = parseToken(token);
            // 解析对应的权限以及用户信息
            String username = claims.get(JwtConstants.USER_NAME, String.class);
            Long userId = claims.get(JwtConstants.USER_ID, Long.class);
            //签发时间
            long issuedAt = claims.getIssuedAt().getTime();
            // token过期时间
            long expirationTime = claims.getExpiration().getTime();
            SwakUserDetails userDetails = userTokenStore.take(userId);
            if (Objects.nonNull(userDetails)) {
                userDetails.setUserId(userId);
                userDetails.setUsername(username);
                userDetails.setToken(token);
                userDetails.setLoginTime(issuedAt);
                userDetails.setExpireTime(expirationTime);
            }
            return userDetails;
        } catch (ExpiredJwtException e) {
            // 异常捕获、发送到ExpiredJwtException
            log.error("token解析用户信息异常", e);
            throw new JwtTokenException("Token凭证已过期");
        } catch (SignatureException e) {
            log.error("token解析用户信息异常", e);
            throw new JwtTokenException("Token凭证签名不正确");
        } catch (Exception e) {
            log.error("token解析用户信息异常", e);
            throw new JwtTokenException("非法的Token凭证");
        }
    }


    @Override
    public JwtTokenConfig getJwtTokenConfig() {
        return jwtTokenConfig;
    }

    @Override
    public UserTokenStore getUserDetailsStore() {
        return userTokenStore;
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

    public void setUserDetailsStore(UserTokenStore userTokenStore) {
        this.userTokenStore = userTokenStore;
    }


    public void setJwtTokenConfig(JwtTokenConfig jwtTokenConfig) {
        this.jwtTokenConfig = jwtTokenConfig;
    }


    public void setSecurityAuthClientService(SecurityAuthClientService securityAuthClientService) {
        this.securityAuthClientService = securityAuthClientService;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        SwakAssert.notNull(securityAuthClientService, "securityAuthClientService cannot be null");
        SwakAssert.notNull(jwtTokenConfig, "jwtTokenConfig cannot be null");
        if (Objects.isNull(userTokenStore)) {
            this.userTokenStore = new DefaultUserTokenStore(securityAuthClientService);
        }
    }
}
