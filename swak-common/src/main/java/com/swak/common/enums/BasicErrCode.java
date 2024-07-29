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
    ACCESS_DENIED(402, "登录态失效，无法访问该资源"),
    NOT_HAVE_PERMISSION(403, "无权限访问，请联系管理员申请对应资源权限！"),
    SWAK_LICENSE(600, "您的试用期授权已失效，请核查系统是否取得授权试用或申请正式授权书！"),
    SWAK_OPERA_REPEAT(601, "你点击的太快啦,稍后再试~"),
    SWAK_REQ_LIMIT(602, "系统繁忙，已优先为您接入快速通道，稍等片刻~"),

    SWAK_ERROR(901, "系统异常，稍后再试~"),
    BIZ_ERROR(902, "业务异常，请稍后再试~"),

    INVALID_PARAMETER(1000, "您输入的参数异常~"),
    PARAMETER_NOT_BLANK(1001, "参数不能为空"),
    PARAMETER_NOT_EMPTY(1002, "参数不能为空"),
    PARAMETER_NOT_NULL(1003, "参数不能为null"),
    PARAMETER_IS_NULL(1004, "参数为null"),
    SAVE_ERROR(1005, "保存失败，请稍后再试~"),
    UPDATE_ERROR(1006, "更新失败，请稍后再试~"),
    DELETE_ERROR(1007, "删除失败，请稍后再试~"),
    ;

    private final Integer code;
    private final String msg;

    BasicErrCode(Integer code, String msg) {
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
