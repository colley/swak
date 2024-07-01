package com.swak.excel.metadata;

import com.swak.common.dto.base.DTO;
import lombok.Data;

import java.util.List;

@Data
public class SheetRowData implements DTO {
    private String sheetName;
    private Class<?> head;
    private List<ExcelRow> excelRows;
}
