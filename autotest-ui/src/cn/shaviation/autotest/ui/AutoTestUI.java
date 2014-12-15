package cn.shaviation.autotest.ui;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.ITestLaunchListener;
import cn.shaviation.autotest.core.TestRunSession;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.ui.internal.views.TestExecutionViewPart;

public class AutoTestUI extends AbstractUIPlugin implements ITestLaunchListener {

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
		AutoTestCore.addTestLaunchListener(this);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		AutoTestCore.removeTestLaunchListener(this);
		plugin = null;
		super.stop(context);
	}

	public static AutoTestUI getDefault() {
		return plugin;
	}

	@Override
	public void onLaunch(final TestRunSession session) {
		getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IViewPart view = getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(TEST_EXECUTION_VIEW_ID);
					if (view instanceof TestExecutionViewPart) {
						((TestExecutionViewPart) view)
								.setActiveTestRunSession(session);
					}
				} catch (PartInitException e) {
					UIUtils.showError(
							getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							"Open Test Session",
							"An error occurred while attaching a test session.",
							e);
				}
			}
		});
	}
}
