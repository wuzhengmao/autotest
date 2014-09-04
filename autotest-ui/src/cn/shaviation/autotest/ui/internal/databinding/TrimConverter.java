package cn.shaviation.autotest.ui.internal.databinding;

import org.eclipse.core.databinding.conversion.Converter;

import cn.shaviation.autotest.util.Objects;

public class TrimConverter extends Converter {

	public TrimConverter() {
		super(String.class, String.class);
	}

	@Override
	public Object convert(Object fromObject) {
		return Objects.toString(fromObject).trim();
	}
}
