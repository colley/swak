package com.swak.security.service;

import com.swak.common.dto.Response;
import com.swak.common.dto.ResponseResult;
import com.swak.security.dto.UserAuthInfo;

import java.util.Collections;
import java.util.Set;

public interface SecurityAuthClientService {

    /**
     * 根据用户名查询用户信息
     * @param username
     */
    Response<UserAuthInfo> loadUserByUsername(String username);


    Response<UserAuthInfo> loadUserById(Long userId);

    /**
     * 根据手机号查询用户信息
     */
    default Response<UserAuthInfo> loadUserByMobile(String mobile) {
        return loadUserByUsername(mobile);
    }

    /**
     * 根据用户ID获取权限信息
     */
    default Set<String> getPermission(Long userId) {
        return Collections.emptySet();
    }

    /**
     * 校验手机验证是否正确
     * @param mobile
     * @param smsCode
     */
    default Response<Void> verifySmsCode(String mobile, String smsCode) {
        return Response.success();
    }


}
