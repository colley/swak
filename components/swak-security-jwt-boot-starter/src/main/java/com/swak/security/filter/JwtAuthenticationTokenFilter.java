package com.swak.security.filter;

import com.swak.core.security.SecurityUtils;
import com.swak.core.security.SwakAuthenticationFilter;
import com.swak.core.security.SwakUserDetails;
import com.swak.security.authentication.UserTokenService;
import com.swak.security.config.JwtConstants;
import com.swak.security.dto.JwtToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

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
            SwakUserDetails loginUser = userTokenService.getUserDetails(token);
            if(Objects.nonNull(loginUser)){
                JwtToken jwtToken = userTokenService.verifyToken(loginUser);
                if(!Objects.equals(loginUser.getToken(),jwtToken.getAccess_token())){
                    // 主动刷新token，并返回给前端
                    String refreshToken = userTokenService.getJwtTokenConfig().getToken().getRefreshToken();
                    response.addHeader(refreshToken, jwtToken.getAccess_token());
                }
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null,
                        loginUser.getAuthorities());
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