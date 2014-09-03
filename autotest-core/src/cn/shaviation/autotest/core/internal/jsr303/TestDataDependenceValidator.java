package cn.shaviation.autotest.core.internal.jsr303;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import cn.shaviation.autotest.core.model.TestStep;
import cn.shaviation.autotest.core.model.TestStepIterator;
import cn.shaviation.autotest.core.model.TestStepIterator.TestStepIteratorException;

public class TestDataDependenceValidator implements
		ConstraintValidator<TestDataDependence, Object> {

	@Override
	public void initialize(TestDataDependence annotation) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		List<TestStep> list = (List<TestStep>) value;
		try {
			TestStepIterator.check(list);
		} catch (TestStepIteratorException e) {
			return false;
		}
		return true;
	}
}
