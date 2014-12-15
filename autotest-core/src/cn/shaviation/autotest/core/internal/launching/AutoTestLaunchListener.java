package cn.shaviation.autotest.core.internal.launching;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import cn.shavation.autotest.runner.TestSessionHelper;
import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.TestRunSession;

public class AutoTestLaunchListener implements ILaunchListener {

	@Override
	public void launchAdded(ILaunch launch) {

	}

	@Override
	public void launchChanged(ILaunch launch) {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		if (config == null) {
			return;
		}
		IJavaProject javaProject = null;
		try {
			String projectName = config.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			if (projectName != null && !projectName.isEmpty()) {
				javaProject = JavaCore.create(ResourcesPlugin.getWorkspace()
						.getRoot().getProject(projectName));
			}
		} catch (CoreException localCoreException) {
		}
		if (javaProject == null) {
			return;
		}
		String portStr = launch
				.getAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_PORT);
		if (portStr == null) {
			return;
		}
		int port = 0;
		try {
			port = Integer.parseInt(portStr);
		} catch (NumberFormatException localNumberFormatException) {
		}
		if (port <= 0) {
			return;
		}
		TestRunSession session = new TestRunSession(launch, javaProject,
				TestSessionHelper.create(port));
		AutoTestCore.setTestRunSession(session);
	}

	@Override
	public void launchRemoved(ILaunch launch) {

	}
}