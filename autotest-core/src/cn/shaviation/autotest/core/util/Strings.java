package cn.shaviation.autotest.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
		if (objs != null) {
			for (Object obj : objs) {
				if (obj != null) {
					if (sb.length() > 0) {
						sb.append(delimiter);
					}
					sb.append(obj.toString());
				}
			}
		}
		return sb.toString();
	}

	public static List<String> split(String text, String delimiter) {
		return split(text, delimiter, String.class, false, false);
	}

	public static <T> List<T> split(String text, String delimiter,
			Class<T> type, boolean ignoreNull, boolean ignoreDuplicate) {
		if (text == null) {
			return Collections.emptyList();
		}
		List<T> list = new ArrayList<T>();
		for (String str : text.split("\\" + delimiter)) {
			T value = null;
			try {
				value = Objects.toObject(str, type);
			} catch (Exception e) {
			}
			if (!ignoreNull || value != null) {
				if (!ignoreDuplicate || !list.contains(value)) {
					list.add(value);
				}
			}
		}
		return list;
	}

	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d*");

	public static boolean isNumber(String str) {
		return NUMBER_PATTERN.matcher(str).matches();
	}
}
