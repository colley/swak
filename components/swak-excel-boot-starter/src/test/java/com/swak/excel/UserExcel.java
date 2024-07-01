package com.swak.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.swak.excel.metadata.BaseRow;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserExcel extends BaseRow {

    @ExcelProperty(value = "姓名")
    private String name;

    @ExcelProperty(value = "年龄")
    private Integer age;
}
