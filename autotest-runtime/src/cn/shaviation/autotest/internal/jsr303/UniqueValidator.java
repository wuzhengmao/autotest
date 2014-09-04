package cn.shaviation.autotest.internal.jsr303;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.UnexpectedTypeException;

import cn.shaviation.autotest.internal.jsr303.Unique.NONE;
import cn.shaviation.autotest.util.Strings;

public class UniqueValidator implements ConstraintValidator<Unique, Object> {

	private Class<?> componentType;
	private String property;

	@Override
	public void initialize(Unique annotation) {
		componentType = annotation.componentType();
		property = annotation.property();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		List<Object> list;
		if (value.getClass().isArray()) {
			list = new ArrayList<Object>();
			for (int i = 0; i < Array.getLength(value); i++) {
				list.add(Array.get(value, i));
			}
		} else if (value instanceof List) {
			list = (List<Object>) value;
		} else if (value instanceof Collection) {
			list = new ArrayList<Object>((Collection<?>) value);
		} else {
			throw new UnexpectedTypeException("Unsupported collection type: "
					+ value.getClass());
		}
		for (Object obj : list) {
			if (obj == null) {
				throw new UnexpectedTypeException(
						"Unsupported component type: null");
			} else if (componentType != null && componentType != NONE.class
					&& !componentType.isAssignableFrom(obj.getClass())) {
				throw new UnexpectedTypeException(
						"Unsupported component type: " + obj.getClass());
			}
		}
		if (list.size() > 1) {
			for (int i = 1; i < list.size(); i++) {
				Object p1 = getValue(list.get(i));
				for (int j = 0; j < i; j++) {
					Object p2 = getValue(list.get(j));
					if ((p1 == null && p2 == null)
							|| (p1 != null && p1.equals(p2))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private Object getValue(Object obj) {
		if (Strings.isEmpty(property)) {
			return obj;
		}
		try {
			PropertyDescriptor pd = new PropertyDescriptor(property,
					obj.getClass());
			return pd.getReadMethod().invoke(obj);
		} catch (Exception e) {
			throw new UnexpectedTypeException("Cannot read property '"
					+ property + "' of component type: " + obj.getClass());
		}
	}
}
