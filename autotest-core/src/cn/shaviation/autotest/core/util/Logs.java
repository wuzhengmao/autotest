package cn.shaviation.autotest.core.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import cn.shaviation.autotest.core.AutoTestCore;

public abstract class Logs {

	public static void e(String message) {
		log(IStatus.ERROR, message, null);
	}

	public static void e(Throwable t) {
		log(IStatus.ERROR, null, t);
	}

	public static void e(String message, Throwable t) {
		log(IStatus.ERROR, message, t);
	}

	public static void w(String message) {
		log(IStatus.WARNING, message, null);
	}

	public static void w(Throwable t) {
		log(IStatus.WARNING, null, t);
	}

	public static void w(String message, Throwable t) {
		log(IStatus.WARNING, message, t);
	}

	public static void i(String message) {
		log(IStatus.INFO, message, null);
	}

	private static void log(int severity, String message, Throwable t) {
		if (t != null) {
			if (t.getCause() != null) {
				t = t.getCause();
			}
			if (message == null || message.isEmpty()) {
				message = t.getMessage();
			}
			if (message == null || message.isEmpty()) {
				message = t.getClass().getName();
			}
		}
		IStatus status = new Status(severity, AutoTestCore.PLUGIN_ID, message,
				t);
		AutoTestCore.getDefault().getLog().log(status);
	}
}
