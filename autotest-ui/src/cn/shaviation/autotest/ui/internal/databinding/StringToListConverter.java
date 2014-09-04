package cn.shaviation.autotest.ui.internal.databinding;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.conversion.Converter;

import cn.shaviation.autotest.util.Strings;

public class StringToListConverter extends Converter {

	private Class<?> type;
	private boolean ignoreNull;
	private boolean ignoreDuplicate;
	private boolean sortable;

	public StringToListConverter(Class<?> type, boolean ignoreNull,
			boolean ignoreDuplicate, boolean sortable) {
		super(String.class, List.class);
		this.type = type;
		this.ignoreNull = ignoreNull;
		this.ignoreDuplicate = ignoreDuplicate;
		this.sortable = sortable;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object convert(Object fromObject) {
		List<?> list = Strings.split((String) fromObject, ",", type,
				ignoreNull, ignoreDuplicate);
		if (sortable) {
			Collections.sort((List<? extends Comparable>) list);
		}
		return list;
	}
}
