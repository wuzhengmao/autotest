package cn.shaviation.autotest.ui.internal.databinding;

import org.eclipse.core.databinding.conversion.Converter;

public abstract class Converters {

	public static final Converter TRIM = new TrimConverter();

	public static final Converter DATESTAMP = new DateFormatConverter(
			"yyyy-MM-dd HH:mm:ss.SSS");

	public static final Converter DATESTAMP_PARSER = new DateFormatParser(
			"yyyy-MM-dd HH:mm:ss.SSS");

}
