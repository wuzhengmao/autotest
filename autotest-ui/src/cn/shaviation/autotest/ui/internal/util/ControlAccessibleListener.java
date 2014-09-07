package cn.shaviation.autotest.ui.internal.util;

import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Control;

public class ControlAccessibleListener extends AccessibleAdapter {

	private String controlName;

	public ControlAccessibleListener(String name) {
		this.controlName = name;
	}

	@Override
	public void getName(AccessibleEvent e) {
		e.result = this.controlName;
	}

	public static void addListener(Control comp, String name) {
		String[] strs = name.split("&");
		StringBuffer stripped = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			stripped.append(strs[i]);
		}
		comp.getAccessible().addAccessibleListener(
				new ControlAccessibleListener(stripped.toString()));
	}
}
