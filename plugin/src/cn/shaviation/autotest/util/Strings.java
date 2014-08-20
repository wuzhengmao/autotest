package cn.shaviation.autotest.util;

import java.util.Collection;

public abstract class Strings {

	public static String objToString(Object value) {
		return value != null ? value.toString() : "";
	}

	public static boolean equals(Object value1, Object value2) {
		String str1 = objToString(value1);
		String str2 = objToString(value2);
		return str1.equals(str2);
	}

	public static String merge(Collection<String> strings, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (String str : strings) {
			if (sb.length() > 0) {
				sb.append(delimiter);
			}
			sb.append(str);
		}
		return sb.toString();
	}
}
