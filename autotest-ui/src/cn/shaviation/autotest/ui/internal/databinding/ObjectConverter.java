package cn.shaviation.autotest.ui.internal.databinding;

import org.eclipse.core.databinding.conversion.Converter;

import cn.shaviation.autotest.core.util.Objects;

public class ObjectConverter extends Converter {

	public ObjectConverter() {
		super(Object.class, String.class);
	}

	@Override
	public Object convert(Object fromObject) {
		return Objects.toString(fromObject);
	}
}
