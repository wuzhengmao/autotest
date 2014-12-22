package cn.shaviation.autotest.core.internal.launching;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.TestRunSession;
import cn.shaviation.autotest.runner.TestSessionHelper;

public class AutoTestLaunchListener implements ILaunchListener {

	private Set<ILaunch> trackedLaunches = new HashSet<ILaunch>(4);

	@Override
	public void launchAdded(ILaunch launch) {
		trackedLaunches.add(launch);
	}

	@Override
	public void launchRemoved(ILaunch launch) {
		trackedLaunches.remove(launch);
	}

	@Override
	public void launchChanged(ILaunch launch) {
		if (!trackedLaunches.contains(launch)) {
			return;
		}
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
		trackedLaunches.remove(launch);
		TestRunSession session = new TestRunSession(launch, javaProject,
				TestSessionHelper.create(port));
		AutoTestCore.setTestRunSession(session);
	}
}
