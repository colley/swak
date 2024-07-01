package com.swak.excel.metadata;

import com.google.common.collect.Maps;
import com.swak.common.dto.base.DTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ExcelSheetData implements DTO {
    private Map<Class<?>,SheetRowData> allSheetRow = Maps.newHashMap();

    public <T> List<T> getSheetRows(Class<T> clazz) {
        SheetRowData sheetRowData = allSheetRow.get(clazz);
        if(Objects.nonNull(sheetRowData)) {
            return (List<T>) sheetRowData.getExcelRows();
        }
       return Collections.emptyList();
    }

    public  void  addSheetRows(SheetRowData sheetRow) {
        allSheetRow.putIfAbsent(sheetRow.getHead(),sheetRow);
    }
}
