package cn.shaviation.autotest.core.util;

import java.text.NumberFormat;
import java.text.ParseException;

public abstract class Objects {

	public static boolean equals(Object obj1, Object obj2) {
		return (obj1 == null && obj2 == null)
				|| (obj1 != null && obj1.equals(obj2));
	}

	public static String toString(Object value) {
		return value != null ? value.toString() : "";
	}

	public static <T> T toObject(String text, Class<T> type) {
		return type.cast(internalToObject(text, type));
	}

	private static Object internalToObject(String text, Class<?> type) {
		try {
			if (type == void.class || type == Void.class) {
				return null;
			} else if (type == String.class) {
				return text;
			} else if (type == char.class || type == Character.class) {
				if (Strings.isEmpty(text)) {
					if (type == char.class) {
						throw new NullPointerException();
					} else {
						return null;
					}
				}
				return text.charAt(0);
			} else if (type == boolean.class || type == Boolean.class) {
				if (Strings.isBlank(text)) {
					if (type == boolean.class) {
						return false;
					} else {
						return null;
					}
				}
				return Boolean.parseBoolean(text.trim());
			}
			text = text != null ? text.trim() : null;
			if (Strings.isEmpty(text)) {
				if (type.isPrimitive()) {
					throw new NullPointerException();
				} else {
					return null;
				}
			} else if (type == long.class || type == Long.class) {
				return NumberFormat.getInstance().parse(text).longValue();
			} else if (type == int.class || type == Integer.class) {
				return NumberFormat.getInstance().parse(text).intValue();
			} else if (type == short.class || type == Short.class) {
				return NumberFormat.getInstance().parse(text).shortValue();
			} else if (type == byte.class || type == Byte.class) {
				return NumberFormat.getInstance().parse(text).byteValue();
			} else if (type == double.class || type == Double.class) {
				return NumberFormat.getInstance().parse(text).doubleValue();
			} else if (type == float.class || type == Float.class) {
				return NumberFormat.getInstance().parse(text).floatValue();
			} else {
				throw new IllegalArgumentException("Unsupported type: "
						+ type.getName());
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
