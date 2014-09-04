package cn.shaviation.autotest.util;

public abstract class Enums {

	public static <T extends Enum<T>> String[] getNames(Class<T> enumType) {
		T[] values = enumType.getEnumConstants();
		String[] names = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			names[i] = values[i].name();
		}
		return names;
	}

	public static <T extends Enum<T>> T getEnum(Class<T> enumType, String name) {
		try {
			return Enum.valueOf(enumType, name);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static <T extends Enum<T>> T getEnum(Class<T> enumType, int ordinal) {
		for (T val : enumType.getEnumConstants()) {
			if (val.ordinal() == ordinal) {
				return val;
			}
		}
		return null;
	}
}
