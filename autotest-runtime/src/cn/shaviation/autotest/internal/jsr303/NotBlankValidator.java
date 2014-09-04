package cn.shaviation.autotest.internal.jsr303;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotBlankValidator implements ConstraintValidator<NotBlank, String> {

	@Override
	public void initialize(NotBlank annotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return !value.trim().isEmpty();
	}
}
