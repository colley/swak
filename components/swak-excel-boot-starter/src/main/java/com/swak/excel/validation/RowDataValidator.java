package com.swak.excel.validation;


import com.swak.common.exception.ExcelException;
import com.swak.common.util.GetterUtil;
import com.swak.common.validation.MessagesFormat;
import com.swak.common.validation.ValidationResult;
import com.swak.excel.enums.ExcelErrCode;
import com.swak.excel.metadata.ExcelRow;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RowDataValidator<T extends ExcelRow> {

    static <T> RowDataValidator<ExcelRow> defaultValidator() {
        return (data) -> new ValidationResult(true);
    }

    ValidationResult validate(T data);

    default boolean validate(T data, Map<String, Integer> cellMap) {
        int rowIndex = data.getRowIndex();
        String sheetName = data.getSheetName();
        ValidationResult validResult = validate(data);
        if (!validResult.isSuccess()) {
            ValidationResult.ErrorMessage errorMessage = validResult.getErrorMessages().get(0);
            Integer columnIndex = cellMap.get(errorMessage.getPropertyName());
            MessagesFormat messagesFormat = new MessagesFormat();
            if (columnIndex != null) {
                Map<Integer, List<String>> rowHeader = Optional.ofNullable(data.getRowHead())
                        .orElse(Collections.emptyMap());
                String propertyNameShow = errorMessage.getPropertyName();
                List<String> propertyNameList = rowHeader.get(columnIndex);
                if (CollectionUtils.isNotEmpty(propertyNameList)) {
                    propertyNameShow = propertyNameList.get(0);
                }

                if (StringUtils.isNotEmpty(sheetName)) {
                    //messagesFormat.setMessage("[%s]-[%s] 第%s行第%s列的值%s");
                    messagesFormat.setResultCode(ExcelErrCode.DATA_ROW_COL_SHEET_ERROR);
                    messagesFormat.setArguments(new Object[]{sheetName, GetterUtil.getString(propertyNameShow, errorMessage.getPropertyName()),
                            (rowIndex + 1), (columnIndex + 1),
                            errorMessage.getMessage()});
                } else {
                    //messagesFormat.setMessage("[%s] 第%s行第%s列的值%s");
                    messagesFormat.setResultCode(ExcelErrCode.DATA_ROW_COL_ERROR);
                    messagesFormat.setArguments(new Object[]{GetterUtil.getString(propertyNameShow, errorMessage.getPropertyName()),
                            (rowIndex + 1), (columnIndex + 1),
                            errorMessage.getMessage()});
                }
            } else {
                if (StringUtils.isNotEmpty(sheetName)) {
                    //messagesFormat.setMessage("[%s]-[%s] 第%s行数据%s");
                    messagesFormat.setResultCode(ExcelErrCode.DATA_ROW_SHEET_ERROR);
                    messagesFormat.setArguments(new Object[]{sheetName, errorMessage.getPropertyName(), (rowIndex + 1), errorMessage.getMessage()});
                } else {
                    //messagesFormat.setMessage("[%s] 第%s行数据%s");
                    messagesFormat.setResultCode(ExcelErrCode.DATA_ROW_ERROR);
                    messagesFormat.setArguments(new Object[]{errorMessage.getPropertyName(), (rowIndex + 1), errorMessage.getMessage()});
                }
            }
            throw new ExcelException(messagesFormat.getResultCode(),messagesFormat.getArguments());
        }
        return true;
    }
}
