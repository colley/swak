package com.swak.excel.enums;

import com.swak.common.enums.IResultCode;

/**
 *
 * @author colley.ma
 * @since 3.0.0
 **/
public enum ExcelErrCode implements IResultCode {
    BIZ_ROW_COL_ERROR(2001, "[{0}] 第{1}行第{2}列的值{3}"),
    BIZ_ROW_ERROR(2002, "[{0}] 第{1}行数据{2}"),

    DATA_ROW_COL_ERROR(2003, "[{0}] 第{1}行第{2}列的值{3}"),
    DATA_ROW_COL_SHEET_ERROR(2004, "[{0}]-[{1}] 第{2}行第{3}列的值{4}"),

    DATA_ROW_ERROR(2005, "[{0}] 第{1}行数据{2}"),
    DATA_ROW_SHEET_ERROR(2006, "[{0}]-[{1}] 第{2}行数据{3}"),

    EXCEL_INVALID_TYPE(2007, "非法的文件格式，只支持xls，xlsx，csv格式"),

    EXCEL_READ_ERROR(2008, "获取excel或csv文件异常！"),

    ;

    private Integer code;
    private String msg;

    ExcelErrCode(Integer code, String msg) {
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
