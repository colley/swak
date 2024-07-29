package com.swak.security.enums;

import com.swak.common.enums.IResultCode;

public enum TokenResultCode implements IResultCode {
    USER_PWD_INCORRECT(1201, "您的密码不正确，请重新输入"),
    USER_NOT_FOUND(1202, "无法找到您的账户信息，请确保账户是否正确"),
    USER_DELETE(1203, "该用户已删除,请联系管理员"),
    USER_LOCK(1204, "用户已被锁住，请联系管理员"),
    VALID_CODE_ERR(1205, "验证码不匹配"),

    TOKEN_EXPIRED(1206, "Token凭证已过期"),

    TOKEN_SIGN(1207, "Token凭证签名不正确"),

    TOKEN_ILLEGAL(1208, "非法的Token凭证"),
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
