package cn.shaviation.autotest.ui.internal.util;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

import cn.shaviation.autotest.core.util.Strings;

public class NumberVerifyListener implements VerifyListener {

	@Override
	public void verifyText(VerifyEvent event) {
		event.doit = Strings.isNumber(event.text);
	}
}
