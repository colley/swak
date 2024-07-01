package com.swak.security.service;

import com.swak.common.dto.Response;
import com.swak.common.dto.ResponseResult;
import com.swak.core.security.SwakUserDetails;

/**
 * UserTokenStore custom
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/11 15:45
 **/
public interface UserTokenStore {

    /**
     * 存储
     *
     * @param userDetails
     */
    Response<Void> store(SwakUserDetails userDetails);

    /**
     * 获取
     *
     * @param userId
     */
    SwakUserDetails take(Long userId);



    /**
     * 刷新
     * @param userDetails
     */
    default void refresh(SwakUserDetails userDetails) {};

    default void remove(Long userId) {};
}
