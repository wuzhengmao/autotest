package cn.shaviation.autotest.internal.jsr303;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import cn.shaviation.autotest.util.Strings;

public class NotBlankValidator implements ConstraintValidator<NotBlank, String> {

	@Override
	public void initialize(NotBlank annotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return !Strings.isBlank(value);
	}
}
