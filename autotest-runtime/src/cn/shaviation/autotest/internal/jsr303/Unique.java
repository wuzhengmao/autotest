package cn.shaviation.autotest.internal.jsr303;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
		ElementType.CONSTRUCTOR, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { UniqueValidator.class })
public @interface Unique {

	interface NONE {};

	Class<?> componentType() default NONE.class;

	String property();

	String message();

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
