package cn.shaviation.autotest.ui.internal.databinding;

import org.eclipse.core.databinding.conversion.Converter;

public abstract class Converters {

	public static final Converter TRIM = new TrimConverter();

	public static final Converter DATESTAMP = new DateFormatConverter(
			"yyyy-MM-dd HH:mm:ss.SSS");

	public static final Converter DATESTAMP_PARSER = new DateFormatParser(
			"yyyy-MM-dd HH:mm:ss.SSS");

	public static final Converter DEFAULT = new ObjectConverter();

	public static final Converter LONG_PARSER = new NumberParser(Long.class);

	public static final Converter INT_PARSER = new NumberParser(Integer.class);

	public static final Converter SHORT_PARSER = new NumberParser(Short.class);

	public static final Converter BYTE_PARSER = new NumberParser(Byte.class);

	public static final Converter DOUBLE_PARSER = new NumberParser(Double.class);

	public static final Converter FLOAT_PARSER = new NumberParser(Float.class);

}
