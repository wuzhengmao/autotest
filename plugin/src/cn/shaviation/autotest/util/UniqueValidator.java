package cn.shaviation.autotest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.PojoProperties;

import cn.shaviation.autotest.util.Unique.NONE;

public class UniqueValidator implements ConstraintValidator<Unique, Object> {

	private Class<?> componentType;
	private String property;

	@Override
	public void initialize(Unique annotation) {
		componentType = annotation.componentType();
		property = annotation.property();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value instanceof Collection && ((Collection<?>) value).size() > 1) {
			IBeanValueProperty bvp = null;
			if (property != null && !property.isEmpty()) {
				bvp = componentType != NONE.class ? PojoProperties.value(
						componentType, property) : PojoProperties
						.value(property);
			}
			List<?> list = new ArrayList<Object>((Collection<?>) value);
			for (int i = 1; i < list.size(); i++) {
				Object p1 = bvp != null ? bvp.getValue(list.get(i)) : list
						.get(i);
				for (int j = 0; j < i; j++) {
					Object p2 = bvp != null ? bvp.getValue(list.get(j)) : list
							.get(j);
					if ((p1 == null && p2 == null)
							|| (p1 != null && p1.equals(p2))) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
