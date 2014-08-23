package cn.shaviation.autotest.ui.internal.databinding;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.databinding.conversion.Converter;

public class DateFormatConverter extends Converter {

	private SimpleDateFormat dateFormat;

	public DateFormatConverter(String pattern) {
		super(Date.class, String.class);
		dateFormat = new SimpleDateFormat(pattern);
	}

	@Override
	public Object convert(Object fromObject) {
		return fromObject != null ? dateFormat.format((Date) fromObject) : null;
	}
}
