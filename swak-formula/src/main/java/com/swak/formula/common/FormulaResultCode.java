package com.swak.formula.common;

import com.swak.common.enums.IResultCode;

/**
 * Formula返回码
 *
 * @author colley.ma
 * @since 2022/9/9 16:24
 */
public enum FormulaResultCode implements IResultCode {
    FORMULA_RUN_ERR(904, "Formula执行异常"),
    FORMULA_COMPILE_ERR(903, "脚本编译失败，请检查脚本是否合法"),
    ;

    private Integer errCode;
    private String errMessage;

	FormulaResultCode(Integer errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    @Override
    public Integer getCode() {
        return errCode;
    }

    @Override
    public String getMsg() {
        return errMessage;
    }

}
