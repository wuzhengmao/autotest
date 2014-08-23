package cn.shaviation.autotest.ui.internal.databinding;

import org.eclipse.core.databinding.conversion.Converter;

public class TrimConverter extends Converter {

	public TrimConverter() {
		super(String.class, String.class);
	}

	@Override
	public Object convert(Object fromObject) {
		return fromObject != null ? fromObject.toString().trim() : null;
	}
}
