package com.swak.security.config;

import com.swak.common.enums.IResultCode;

public enum TokenResultCode implements IResultCode {
    USER_PWD_INCORRECT(4401, "您的密码不正确，请重新输入"),
    USER_NOT_FOUND(4403, "无法找到您的账户信息，请确保账户是否正确"),
    USER_DELETE(4405, "该用户已删除,请联系管理员"),
    USER_LOCK(4406, "用户已被锁住，请联系管理员"),
    VALID_CODE_ERR(4408, "验证码不匹配"),
    ACCESS_DENIED(401, "登录态失效，无法访问该资源"),
    ;

    private final Integer code;
    private final String msg;

    TokenResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

}
