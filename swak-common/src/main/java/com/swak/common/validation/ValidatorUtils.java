
package com.swak.common.validation;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;

public class ValidatorUtils {

    private static Validator validator;

    private static Validator defaultValidator;

    public static void setValidator(Validator _validator) {
        ValidatorUtils.validator = _validator;
    }

    /**
     * 获取原生{@link Validator} 对象
     *
     * @return {@link Validator} 对象
     */
    public static Validator getValidator() {
        return Optional.ofNullable(validator).orElse(getDefaultValidator());
    }

    private static Validator getDefaultValidator() {
		return Optional.ofNullable(defaultValidator).orElse(defaultValidator =
				Validation.buildDefaultValidatorFactory().getValidator());
    }

    /**
     * 校验对象
     *
     * @param <T>    Bean类型
     * @param bean   bean
     * @param groups 校验组
     * @return {@link Set}
     */
    public static <T> Set<ConstraintViolation<T>> validate(T bean, Class<?>... groups) {
        return getValidator().validate(bean, groups);
    }

    /**
     * 校验bean的某一个属性
     *
     * @param <T>          Bean类型
     * @param bean         bean
     * @param propertyName 属性名称
     * @param groups       验证分组
     * @return {@link Set}
     */
    public static <T> Set<ConstraintViolation<T>> validateProperty(T bean, String propertyName, Class<?>... groups) {
        return getValidator().validateProperty(bean, propertyName, groups);
    }

    /**
     * 校验对象
     *
     * @param <T>    Bean类型
     * @param bean   bean
     * @param groups 校验组
     * @return {@link ValidationResult}
     */
    public static <T> ValidationResult warpValidate(T bean, Class<?>... groups) {
        return warpBeanValidationResult(validate(bean, groups));
    }

    /**
     * 校验bean的某一个属性
     *
     * @param <T>          bean类型
     * @param bean         bean
     * @param propertyName 属性名称
     * @param groups       验证分组
     * @return {@link ValidationResult}
     */
    public static <T> ValidationResult warpValidateProperty(T bean, String propertyName, Class<?>... groups) {
        return warpBeanValidationResult(validateProperty(bean, propertyName, groups));
    }

    /**
     * 包装校验结果
     *
     * @param constraintViolations 校验结果集
     * @return {@link ValidationResult}
     */
    private static <T> ValidationResult warpBeanValidationResult(Set<ConstraintViolation<T>> constraintViolations) {
        ValidationResult result = new ValidationResult(constraintViolations.isEmpty());
        for (ConstraintViolation<T> constraintViolation : constraintViolations) {
            ValidationResult.ErrorMessage errorMessage = new ValidationResult.ErrorMessage();
            errorMessage.setPropertyName(constraintViolation.getPropertyPath().toString());
            errorMessage.setMessage(constraintViolation.getMessage());
            errorMessage.setValue(constraintViolation.getInvalidValue());
            result.addErrorMessage(errorMessage);
        }
        return result;
    }
}
