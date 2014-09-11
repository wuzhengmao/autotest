package cn.shaviation.autotest.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class AutoTestUI extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "cn.shaviation.autotest.ui"; //$NON-NLS-1$
	public static final String TEST_DATA_EDITOR_ID = "cn.shaviation.autotest.ui.editors.TestDataEditor"; //$NON-NLS-1$
	public static final String TEST_SCRIPT_EDITOR_ID = "cn.shaviation.autotest.ui.editors.TestScriptEditor"; //$NON-NLS-1$
	public static final String TEST_EXECUTION_VIEW_ID = "cn.shaviation.autotest.ui.views.TestExecutionViewPart"; //$NON-NLS-1$
	public static final String LAUNCH_SHORTCUT_ID = "cn.shaviation.autotest.launching.AutoTestLaunchShortcut"; //$NON-NLS-1$

	private static AutoTestUI plugin;

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

	public static AutoTestUI getDefault() {
		return plugin;
	}
}
