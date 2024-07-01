package com.swak.excel.validation;


import com.swak.common.validation.ValidationResult;
import com.swak.common.validation.ValidatorUtils;
import com.swak.excel.metadata.ExcelRow;

public class BeanDataValidator<T extends ExcelRow> implements RowDataValidator {

    private Class<?>[] groups;

    public BeanDataValidator(Class<?>[] groups) {
        this.groups = groups;
    }

    public static <T> BeanDataValidator<ExcelRow> newDataValidator(Class<?>[] groups) {
        return new BeanDataValidator<ExcelRow>(groups);
    }

    @Override
    public ValidationResult validate(ExcelRow data) {
        return ValidatorUtils.warpValidate(data, groups);
    }
}
