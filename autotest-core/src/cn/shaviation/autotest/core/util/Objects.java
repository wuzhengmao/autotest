package cn.shaviation.autotest.core.util;

public abstract class Objects {

	public static boolean equals(Object obj1, Object obj2) {
		return (obj1 == null && obj2 == null)
				|| (obj1 != null && obj1.equals(obj2));
	}

	public static String toString(Object value) {
		return value != null ? value.toString() : "";
	}
}
