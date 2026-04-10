package com.swak.security.filter;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.swak.common.util.GetterUtil;
import com.swak.security.authentication.SmsAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class DynamicAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private String usernameParameter = "username";
    private String passwordParameter = "password";

    private String mobileParameter = "mobile";
    private String smsCodeParameter = "smsCode";

    private boolean postOnly = true;


    public DynamicAuthenticationFilter(String loginUrl) {
        super(new AntPathRequestMatcher(loginUrl, "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("[Swak-Security] Authentication method not supported: " + request.getMethod());
        }
        String contentType = request.getContentType();
        String mobile = null;
        String smsCode = null;
        // 判断是否为JSON
        if (contentType != null && contentType.contains("application/json")) {
            // 处理JSON数据
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            if(StringUtils.isNotEmpty(body)) {
                JSONObject jsonObject = JSON.parseObject(body);
                mobile = jsonObject.getString(mobileParameter);
                smsCode = jsonObject.getString(smsCodeParameter);
            }
        } else {
            // 不是JSON数据的处理逻辑
             mobile = GetterUtil.getString(request.getParameter(mobileParameter));
             smsCode = GetterUtil.getString(request.getParameter(smsCodeParameter));
        }
        if(StringUtils.isNoneBlank(mobile,smsCode)){
            return doSmsAttemptAuthentication(mobile,smsCode,request, response);
        }
        return doAttemptAuthentication(request, response);
    }

    public Authentication doSmsAttemptAuthentication(String mobile,String smsCode, HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        SmsAuthenticationToken authRequest = new SmsAuthenticationToken(mobile, smsCode);
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    public Authentication doAttemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = GetterUtil.getString(this.obtainUsername(request));
        String password = GetterUtil.getString(this.obtainPassword(request));
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }

    @Nullable
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(this.passwordParameter);
    }

    @Nullable
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(this.usernameParameter);
    }


    public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "[Swak-Security] Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }

    public void setPasswordParameter(String passwordParameter) {
        Assert.hasText(passwordParameter, "[Swak-Security] Password parameter must not be empty or null");
        this.passwordParameter = passwordParameter;
    }

    public void setMobileParameter(String mobileParameter) {
        Assert.hasText(mobileParameter, "[Swak-Security] mobile parameter must not be empty or null");
        this.mobileParameter = mobileParameter;
    }

    public void setSmsCodeParameter(String smsCodeParameter) {
        Assert.hasText(smsCodeParameter, "[Swak-Security] smsCode parameter must not be empty or null");
        this.smsCodeParameter = smsCodeParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }
}