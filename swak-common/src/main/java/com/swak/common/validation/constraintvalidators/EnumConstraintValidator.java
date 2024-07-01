package com.swak.common.validation.constraintvalidators;

import com.swak.common.validation.constraints.EnumValid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Objects;


/**
 * The type Enum constraint validator.
 * @author colley.ma
 * @since 2022/9/13 16:20
 */
@Slf4j
public class EnumConstraintValidator implements ConstraintValidator<EnumValid, CharSequence> {

	private final static String METHOD_NAME = "getValue";

	private Class<? extends Enum<?>> enumCls;

	private boolean validEmpty;

	@Override
	public void initialize(EnumValid enumValid) {
		this.enumCls = enumValid.value();
		this.validEmpty = enumValid.notEmpty();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if(!validEmpty && StringUtils.isEmpty(value)){
			return true;
		}
		if (validEmpty && StringUtils.isEmpty(value)) {
			return false;
		}

		if (!enumCls.isEnum()) {
			return false;
		}

		boolean isCheck = enumCls.isEnum() && StringUtils.isNotEmpty(value);
		if (!isCheck) {
			return false;
		}
		boolean result = false;
		// 获取枚举
		Object[] objects = enumCls.getEnumConstants();
		try {
			// 获取方法
			Method method = enumCls.getMethod(METHOD_NAME);
			// 循环枚举
			for (Object obj : objects) {
				// 执行方法
				Object code = method.invoke(obj);
				// 对比值
				if (Objects.deepEquals(value, code)) {
					result = true;
					break;
				}
			}
		} catch (Exception e) {
			log.error("EnumConstraintValidator call isValid() method exception", e);
		}
		return result;
	}

}
