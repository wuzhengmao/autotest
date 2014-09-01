package cn.shaviation.autotest.core.util;

import java.util.Collection;
import java.util.regex.Pattern;

public abstract class Strings {

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean equals(Object value1, Object value2) {
		String str1 = Objects.toString(value1);
		String str2 = Objects.toString(value2);
		return str1.equals(str2);
	}

	public static String merge(Collection<?> objs, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (Object obj : objs) {
			if (obj != null) {
				if (sb.length() > 0) {
					sb.append(delimiter);
				}
				sb.append(obj.toString());
			}
		}
		return sb.toString();
	}

	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d*");

	public static boolean isNumber(String str) {
		return NUMBER_PATTERN.matcher(str).matches();
	}
}
