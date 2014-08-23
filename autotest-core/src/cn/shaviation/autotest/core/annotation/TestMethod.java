package cn.shaviation.autotest.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface TestMethod {

	String value() default "";

	String description() default "";

	String author() default "";

	String version() default "1.0.0";
}
