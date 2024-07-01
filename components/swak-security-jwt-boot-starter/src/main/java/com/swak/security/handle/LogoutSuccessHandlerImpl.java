package com.swak.security.handle;


import com.swak.common.dto.Response;
import com.swak.common.dto.ResponseResult;
import com.swak.common.util.ResponseRender;
import com.swak.core.security.AuthenticationListener;
import com.swak.core.security.SwakUserDetails;
import com.swak.security.authentication.UserTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 自定义退出处理类 返回成功
 */

public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final UserTokenService userTokenService;

    private List<AuthenticationListener> authenticationListeners = new ArrayList<>();

    public LogoutSuccessHandlerImpl(UserTokenService userTokenService,List<AuthenticationListener> authenticationListeners) {
        this.userTokenService = userTokenService;
        if (!CollectionUtils.isEmpty(authenticationListeners)) {
            this.authenticationListeners.addAll(authenticationListeners);
        }
    }

    /**
     * 退出处理
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SwakUserDetails userDetails = (SwakUserDetails) authentication.getPrincipal();
        if(Objects.nonNull(userDetails)){
            userTokenService.getUserDetailsStore().remove(userDetails.getUserId());
            authenticationListeners.forEach(listener -> listener.onLogoutSuccess(request,response));
        }
        ResponseRender.renderJson(Response.success(),response);
    }
}
