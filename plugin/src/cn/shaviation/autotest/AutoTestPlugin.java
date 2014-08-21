package cn.shaviation.autotest;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class AutoTestPlugin extends AbstractUIPlugin {

	public static final String ID = "cn.shaviation.autotest"; //$NON-NLS-1$

	private static AutoTestPlugin plugin;
	
	public AutoTestPlugin() {

	}

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

	public static AutoTestPlugin getDefault() {
		return plugin;
	}
}
