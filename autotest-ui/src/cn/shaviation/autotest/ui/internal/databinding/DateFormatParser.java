package cn.shaviation.autotest.ui.internal.databinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.databinding.conversion.Converter;

public class DateFormatParser extends Converter {

	private SimpleDateFormat dateFormat;

	public DateFormatParser(String pattern) {
		super(String.class, Date.class);
		dateFormat = new SimpleDateFormat(pattern);
	}

	@Override
	public Object convert(Object fromObject) {
		try {
			return fromObject != null ? dateFormat.parse(fromObject.toString())
					: null;
		} catch (ParseException e) {
			return null;
		}
	}
}
