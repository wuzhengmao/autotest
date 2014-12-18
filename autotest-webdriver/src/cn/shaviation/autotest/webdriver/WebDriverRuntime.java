package cn.shaviation.autotest.webdriver;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class WebDriverRuntime extends Plugin {

	public static final String EXTENSION_ID = "webdrv"; //$NON-NLS-1$
	public static final String EXTENSION_NAME = "WebDriver"; //$NON-NLS-1$

	private static Plugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Plugin getDefault() {
		return plugin;
	}
}
