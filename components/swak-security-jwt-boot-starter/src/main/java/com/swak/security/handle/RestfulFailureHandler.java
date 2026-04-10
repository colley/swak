package com.swak.security.handle;

import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.common.util.ResponseRender;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestfulFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        Response<Void> result = Response.build(BasicErrCode.UNAUTHENTICATED);
        if (e instanceof LockedException) {
            result.setMsg("账户被锁定，请联系管理员!");
        } else if (e instanceof CredentialsExpiredException) {
            result.setMsg("密码过期，请联系管理员!");
        } else if (e instanceof AccountExpiredException) {
            result.setMsg("账户过期，请联系管理员!");
        } else if (e instanceof DisabledException) {
            result.setMsg("账户被禁用，请联系管理员!");
        } else if (e instanceof BadCredentialsException) {
            result.setMsg("用户名或者密码输入错误，请重新输入!");
        }
        ResponseRender.renderJackson(result,response);
    }
}
