package cn.shaviation.autotest.util;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

public abstract class Validators {

	private static ValidatorFactory factory;

	private static ValidatorFactory getValidatorFactory() {
		if (factory == null) {
			synchronized (ValidatorFactory.class) {
				if (factory == null) {
					factory = Validation
							.byDefaultProvider()
							.configure()
							.messageInterpolator(
									new ResourceBundleMessageInterpolator(
											new PlatformResourceBundleLocator(
													"validationMessages")))
							.buildValidatorFactory();
				}
			}
		}
		return factory;
	}

	public static <T> Set<ConstraintViolation<T>> validate(T bean,
			Class<?>... classes) {
		Validator validator = getValidatorFactory().getValidator();
		return validator.validate(bean, classes);
	}

	public static <T> Set<ConstraintViolation<T>> validateProperty(T bean,
			String propName, Class<?>... classes) {
		Validator validator = getValidatorFactory().getValidator();
		return validator.validateProperty(bean, propName, classes);
	}

	public static <T> Set<ConstraintViolation<T>> validateValue(Class<T> clazz,
			String propName, Object value) {
		Validator validator = getValidatorFactory().getValidator();
		return validator.validateValue(clazz, propName, value);
	}
}
