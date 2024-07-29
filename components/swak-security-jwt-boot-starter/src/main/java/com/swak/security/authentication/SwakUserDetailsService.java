package com.swak.security.authentication;

import com.swak.common.dto.Response;
import com.swak.core.security.SwakUserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

public interface SwakUserDetailsService extends UserDetailsService {

    /**
     * 通过手机号查询用户信息
     * @return SwakUserDetails
     */
    SwakUserDetails loadUserByMobile(String mobile) throws UsernameNotFoundException;

    /**
     * 手机短信验证匹配
     */
    default Response<Void> verifySmsCode(String mobile, String smsCode) {
        return Response.success();
    }

    /**
     * 权限获取
     * @return Set<String>
     */
    Set<String> getPermission(Long userId);
}
