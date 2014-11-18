package cn.shaviation.autotest.core.internal.launching;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.RefreshUtil;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import cn.shavation.autotest.AutoTest;
import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.util.Logs;
import cn.shaviation.autotest.util.Strings;

public class AutoTestLaunchDelegate extends JavaLaunchDelegate {

	@Override
	public String getMainTypeName(ILaunchConfiguration configuration)
			throws CoreException {
		return AutoTest.class.getName();
	}

	@Override
	public String getProgramArguments(ILaunchConfiguration configuration)
			throws CoreException {
		StringBuilder sb = new StringBuilder();
		String projectName = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		if (Strings.isBlank(projectName)) {
			throw new CoreException(new Status(IStatus.ERROR,
					AutoTestCore.PLUGIN_ID, "Project not specified"));
		}
		sb.append("-j ").append(Strings.encodeUrl(projectName.trim()))
				.append(" ");
		if (configuration.getAttribute(
				AutoTestCore.LAUNCH_CONFIG_ATTR_RECURSIVE, true)) {
			sb.append("-r ");
		}
		IFolder folder = getLogFolder(configuration);
		if (folder != null) {
			sb.append("-l ")
					.append(Strings.encodeUrl(folder.getRawLocation()
							.toOSString())).append(" ");
		}
		sb.append("-c ").append(getProject(configuration).getDefaultCharset())
				.append(" ");
		int port = evaluatePort();
		sb.append("-p ").append(port).append(" ");
		String location = configuration.getAttribute(
				AutoTestCore.LAUNCH_CONFIG_ATTR_LOCATION, "");
		if (Strings.isBlank(location)) {
			throw new CoreException(new Status(IStatus.ERROR,
					AutoTestCore.PLUGIN_ID, "Test resource not specified"));
		}
		for (String resource : Strings.split(location, ",")) {
			sb.append(Strings.encodeUrl(resource.trim()));
		}
		return sb.toString();
	}

	@Override
	public IVMRunner getVMRunner(final ILaunchConfiguration configuration,
			String mode) throws CoreException {
		final IFolder folder = getLogFolder(configuration);
		if (folder != null) {
			final IVMRunner runner = super.getVMRunner(configuration, mode);
			return new IVMRunner() {
				@Override
				public void run(VMRunnerConfiguration runnerConfiguration,
						ILaunch launch, IProgressMonitor monitor)
						throws CoreException {
					runner.run(runnerConfiguration, launch, monitor);
					IProcess[] processes = launch.getProcesses();
					if (processes != null && processes.length > 0) {
						BackgroundResourceRefresher refresher = new BackgroundResourceRefresher(
								folder, launch);
						refresher.init();
					}
				}
			};
		} else {
			return super.getVMRunner(configuration, mode);
		}
	}

	private IProject getProject(ILaunchConfiguration configuration)
			throws CoreException {
		return getJavaProject(configuration).getProject();
	}

	private IFolder getLogFolder(ILaunchConfiguration configuration)
			throws CoreException {
		String logPath = configuration.getAttribute(
				AutoTestCore.LAUNCH_CONFIG_ATTR_LOG_PATH, "");
		if (!Strings.isBlank(logPath)) {
			IProject project = getProject(configuration);
			IFolder folder;
			if (!logPath.startsWith("file:")) {
				folder = (IFolder) project.getWorkspace().getRoot()
						.getFolder(new Path(logPath.trim()));
			} else {
				folder = (IFolder) project
						.getWorkspace()
						.getRoot()
						.getFolder(
								new Path(logPath.substring(5).trim())
										.makeRelativeTo(project.getWorkspace()
												.getRoot().getRawLocation()));
			}
			if (!folder.exists()) {
				folder.create(true, false, null);
			}
			return folder;
		}
		return null;
	}

	private int evaluatePort() throws CoreException {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR,
				AutoTestCore.PLUGIN_ID, "No socket available"));
	}

	public static class BackgroundResourceRefresher implements
			IDebugEventSetListener {
		private IContainer container;
		private IProcess process;

		public BackgroundResourceRefresher(IContainer container, ILaunch launch) {
			this.container = container;
			this.process = launch.getProcesses()[0];
		}

		public void init() {
			synchronized (this.process) {
				if (this.process.isTerminated()) {
					processResources();
				} else {
					DebugPlugin.getDefault().addDebugEventListener(this);
				}
			}
		}

		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				DebugEvent event = events[i];
				if ((event.getSource() == this.process)
						&& (event.getKind() == DebugEvent.TERMINATE)) {
					DebugPlugin.getDefault().removeDebugEventListener(this);
					processResources();
					break;
				}
			}
		}

		private void processResources() {
			Job job = new Job("Refreshing resources...") {
				public IStatus run(IProgressMonitor monitor) {
					try {
						RefreshUtil.refreshResources(
								new IResource[] { container },
								IResource.DEPTH_INFINITE, monitor);
						return Status.OK_STATUS;
					} catch (CoreException e) {
						Logs.e(e);
						return e.getStatus();
					}
				}
			};
			job.schedule();
		}
	}
}
