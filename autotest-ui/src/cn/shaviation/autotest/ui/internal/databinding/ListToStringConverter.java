package cn.shaviation.autotest.ui.internal.databinding;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.conversion.Converter;

import cn.shaviation.autotest.util.Strings;

public class ListToStringConverter extends Converter {

	private boolean sortable;

	public ListToStringConverter(boolean sortable) {
		super(List.class, String.class);
		this.sortable = sortable;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object convert(Object fromObject) {
		if (sortable && fromObject != null) {
			Collections.sort((List<? extends Comparable>) fromObject);
		}
		return Strings.merge((List<?>) fromObject, ",");
	}
}
