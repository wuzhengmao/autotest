package cn.shaviation.autotest.ui.internal.databinding;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.core.databinding.conversion.Converter;

import cn.shaviation.autotest.core.util.Objects;
import cn.shaviation.autotest.core.util.Strings;

public class NumberParser extends Converter {

	public NumberParser(Class<? extends Number> klass) {
		super(String.class, klass);
	}

	@Override
	public Object convert(Object fromObject) {
		Number number = null;
		try {
			String source = Objects.toString(fromObject);
			if (!Strings.isBlank(source)) {
				number = NumberFormat.getInstance()
						.parse(fromObject.toString());
				Class<?> type = (Class<?>) getToType();
				if (type == Long.class || type == long.class) {
					return number.longValue();
				} else if (type == Integer.class || type == int.class) {
					return number.intValue();
				} else if (type == Short.class || type == short.class) {
					return number.shortValue();
				} else if (type == Byte.class || type == byte.class) {
					return number.byteValue();
				} else if (type == Double.class || type == double.class) {
					return number.doubleValue();
				} else if (type == Float.class || type == float.class) {
					return number.floatValue();
				}
			}
		} catch (ParseException e) {
		}
		return null;
	}
}
