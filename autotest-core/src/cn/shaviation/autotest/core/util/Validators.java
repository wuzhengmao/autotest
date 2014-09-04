package cn.shaviation.autotest.core.util;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.engine.ConfigurationImpl;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

public abstract class Validators {

	private static ValidatorFactory factory;

	private static ValidatorFactory getValidatorFactory() {
		if (factory == null) {
			synchronized (ValidatorFactory.class) {
				if (factory == null) {
					factory = new ConfigurationImpl(new HibernateValidator())
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

	public static <T> String getErrorMessage(
			Set<ConstraintViolation<T>> violations) {
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<T> violation : violations) {
				if (sb.length() > 0)
					sb.append("\n");
				sb.append(violation.getMessage());
			}
			return sb.toString();
		}
		return null;
	}
}
