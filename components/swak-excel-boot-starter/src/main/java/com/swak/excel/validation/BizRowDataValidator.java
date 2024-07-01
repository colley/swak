package com.swak.excel.validation;


import com.swak.common.exception.ExcelException;
import com.swak.common.validation.ValidationResult;
import com.swak.excel.enums.ExcelErrCode;

import java.util.List;
import java.util.Map;

public interface BizRowDataValidator<T> {

    ValidationResult validate(List<T> dataList);

    default boolean validate(List<T> dataList, Map<String, Integer> cellMap) {
        ValidationResult validResult = validate(dataList);
        int rowIndex = validResult.getRowIndex();
        if (!validResult.isSuccess()) {
            ValidationResult.ErrorMessage errorMessage = validResult.getErrorMessages().get(0);
            Integer columnIndex = cellMap.get(errorMessage.getPropertyName());
            if (columnIndex != null) {
                throw new ExcelException(ExcelErrCode.BIZ_ROW_COL_ERROR, new Object[]{errorMessage.getPropertyName(),
                        (rowIndex + 1), (columnIndex + 1), errorMessage.getMessage()});
            } else {
                throw new ExcelException(ExcelErrCode.BIZ_ROW_ERROR, new Object[]{errorMessage.getPropertyName(),
                        (rowIndex + 1), errorMessage.getMessage()});
            }
        }
        return true;
    }
}
