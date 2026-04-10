package com.swak.security.handle;

import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.common.util.GetterUtil;
import com.swak.common.util.ResponseRender;
import com.swak.security.exception.JwtTokenException;
import com.swak.security.exception.UserAccountException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * 认证失败处理类 返回未授权
 */
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        String requestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
        Exception lastException = Objects.nonNull(exception)? exception : e;
        Response<String> result = Response.build(BasicErrCode.ACCESS_DENIED);
        if(lastException instanceof JwtTokenException ) {
            JwtTokenException accountException = (JwtTokenException) lastException;
            result.setMsg(accountException.getMessage());
        }
        if (lastException instanceof InternalAuthenticationServiceException){
            InternalAuthenticationServiceException internalException = (InternalAuthenticationServiceException) lastException;
            if (exception.getCause()!=null) {
                //判断抛出的是否是自己定义的异常
                if (internalException.getCause() instanceof UserAccountException){
                    //自定义异常
                    UserAccountException accountException= (UserAccountException) internalException.getCause();
                    result.apply(accountException.getCode()).apply(accountException.getMessage());
                }
            }
        }
        result.setData(GetterUtil.getString(requestUri,request.getRequestURI()));
        ResponseRender.renderJson(result,response);
    }
}
