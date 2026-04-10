package com.swak.security.filter;

import com.swak.core.security.SecurityUtils;
import com.swak.core.security.SwakAuthenticationFilter;
import com.swak.core.security.SwakUserDetails;
import com.swak.core.security.TokenJwtDetails;
import com.swak.security.authentication.UserTokenService;
import com.swak.security.config.JwtConstants;
import com.swak.security.converter.UserDetailsConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * token过滤器 验证token有效性
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private UserTokenService userTokenService;

    @Resource
    private UserDetailsConverter userDetailsConverter;

    private  List<SwakAuthenticationFilter> authenticationFilters = new ArrayList<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = userTokenService.getToken(request);
        if(StringUtils.isEmpty(token)){
            chain.doFilter(request, response);
            return;
        }
        // 如果请求路径是放行路径，则直接跳过认证
        if(matches(request)){
            chain.doFilter(request, response);
            return;
        }
        if(Objects.isNull(SecurityUtils.getAuthentication())){
            TokenJwtDetails tokenJwtDetails = userTokenService.getUserDetails(token);
            if(Objects.nonNull(tokenJwtDetails)){
               userTokenService.verifyToken(tokenJwtDetails);
                SwakUserDetails swakUserDetails = userDetailsConverter.toUserDetails(tokenJwtDetails);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(swakUserDetails, null,
                        swakUserDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                for (SwakAuthenticationFilter tokenFilter : authenticationFilters) {
                    tokenFilter.doFilter(request,response);
                }
            }
        }
        chain.doFilter(request, response);
    }

    public void setUserTokenService(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
    }

    public void setAuthenticationFilters(List<SwakAuthenticationFilter> authenticationFilters) {
        if(Objects.nonNull(authenticationFilters)){
            this.authenticationFilters = authenticationFilters;
        }
    }

    private boolean matches(HttpServletRequest request){
        Set<String> anonUrlList = new HashSet<>();
        anonUrlList.addAll(Arrays.asList(JwtConstants.AuthWhiteList.AUTH_WHITELIST));
        anonUrlList.addAll(Arrays.asList(JwtConstants.AuthWhiteList.STATIC_WHITELIST));
        for (String path : anonUrlList) {
            if (new AntPathRequestMatcher(path).matches(request)) {
                return true;
            }
        }
        return false;
    }
}