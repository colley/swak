package com.swak.common.enums;

/**
 * 基本的返回码
 *
 * @author colley.ma
 * @since 2022/9/9 16:24
 */
public enum BasicErrCode implements IResultCode {
    SUCCESS(0, "OK"),

    USER_NO_LOGIN(401, "您未登录，请先登录~"),
    UNAUTHENTICATED(401, "Unauthenticated"),
    NOT_HAVE_PERMISSION(403, "无权限访问，请联系管理员申请对应资源权限！"),
    SWAK_ERROR(900, "系统异常，稍后再试~"),
    BIZ_ERROR(901, "业务异常，请稍后再试~"),

    SWAK_LICENSE(904, "您的试用期授权已失效，请核查系统是否取得授权试用或申请正式授权书！"),

    SWAK_OPERA_REPEAT(1001, "你点击的太快啦,稍后再试~"),
    SWAK_REQ_LIMIT(1002, "系统繁忙，已优先为您接入快速通道，稍等片刻~"),

    INVALID_PARAMETER(700, "您输入的参数异常~"),

    PARAMETER_NOT_BLANK(700,"不能为空"),
    PARAMETER_NOT_EMPTY(701,"不能为空"),
    PARAMETER_NOT_NULL(702,"不能为null"),
    PARAMETER_IS_NULL(703,"为null"),
    ;

    private Integer code;
    private String msg;

    private BasicErrCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static void main(String[] args) {
        System.out.println(BasicErrCode.SUCCESS.getI18nMsg());
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
