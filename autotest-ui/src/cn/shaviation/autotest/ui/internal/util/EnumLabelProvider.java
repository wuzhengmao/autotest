package cn.shaviation.autotest.ui.internal.util;

import org.eclipse.jface.viewers.LabelProvider;

public class EnumLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		return ((Enum<?>) element).name();
	}
}
