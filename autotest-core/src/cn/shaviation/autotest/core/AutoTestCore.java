package cn.shaviation.autotest.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class AutoTestCore extends Plugin {

	public static final String PLUGIN_ID = "cn.shaviation.autotest.core"; //$NON-NLS-1$
	public static final String NATURE_ID = "cn.shaviation.autotest.core.nature"; //$NON-NLS-1$
	public static final String BUILDER_ID = "cn.shaviation.autotest.core.builder"; //$NON-NLS-1$
	public static final String TEST_DATA_FILE_EXTENSION = "tdd"; //$NON-NLS-1$

	private static AutoTestCore plugin;

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

	public static AutoTestCore getDefault() {
		return plugin;
	}
}