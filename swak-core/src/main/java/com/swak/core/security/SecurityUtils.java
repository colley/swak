package com.swak.core.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.PatternMatchUtils;

import java.util.*;

/**
 * 安全服务工具类
 * @author colley.ma
 */
public class SecurityUtils {
    /**
     * 所有权限标识
     */
    private static final String ALL_PERMISSION = "*:*:*";


    /**
     * 获取用户账户
     **/
    public static String getUserName() {
        return Optional.ofNullable(getUserDetails()).map(SwakUserDetails::getUsername).orElse(null);
    }

    public static Long getUserId() {
        return Optional.ofNullable(getUserDetails()).map(SwakUserDetails::getUserId).orElse(null);
    }

    /**
     * 获取用户
     **/
    public static SwakUserDetails getUserDetails() {
        Authentication  authentication = getAuthentication();
        if(Objects.isNull(authentication)){
            return null;
        }
        Object principal = authentication.getPrincipal();
        if(principal instanceof  SwakUserDetails) {
            return (SwakUserDetails) principal;
        }
        return null;
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public static boolean hasPermission(String permission) {
        Set<String> permissionSet = Optional.ofNullable(getUserDetails())
                .map(SwakUserDetails::getPermissions).orElse(Collections.emptySet());
        return hasPermission(permissionSet, permission);
    }

    /**
     * 判断是否包含权限
     *
     * @param authorities 权限列表
     * @param permission  权限字符串
     * @return 用户是否具备某权限
     */
    public static boolean hasPermission(Collection<String> authorities, String permission) {
        return authorities.stream().filter(StringUtils::isNotBlank)
                .anyMatch(x -> ALL_PERMISSION.equals(x) || PatternMatchUtils.simpleMatch(x, permission));
    }

}
