package com.swak.common.validation.constraints;

import com.swak.common.validation.constraintvalidators.EnumConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumConstraintValidator.class)
@Documented
public @interface EnumValid {

	Class<?extends Enum<?>> value();
	
	 boolean notEmpty() default false;

	String message() default "{com.swak.common.validation.constraints.EnumValid.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
