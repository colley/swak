package com.swak.security.handle;

import com.swak.common.dto.Response;
import com.swak.common.enums.BasicErrCode;
import com.swak.common.util.ResponseRender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestfulAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException, ServletException {
        ResponseRender.renderJson(Response.fail(BasicErrCode.NOT_HAVE_PERMISSION),response);
    }
}
