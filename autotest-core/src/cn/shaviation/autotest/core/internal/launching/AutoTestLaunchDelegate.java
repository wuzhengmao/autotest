package cn.shaviation.autotest.core.internal.launching;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.RefreshUtil;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import cn.shavation.autotest.AutoTest;
import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.util.Logs;

public class AutoTestLaunchDelegate extends JavaLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProject project = getJavaProject(configuration).getProject();
		IFolder logs = project.getFolder("logs");
		if (!logs.exists()) {
			logs.create(true, false, null);
		}
		super.launch(configuration, mode, launch, monitor);
	}

	@Override
	public String getMainTypeName(ILaunchConfiguration configuration)
			throws CoreException {
		return AutoTest.class.getName();
	}

	@Override
	public String getProgramArguments(ILaunchConfiguration configuration)
			throws CoreException {
		StringBuilder sb = new StringBuilder();
		if (configuration.getAttribute(
				AutoTestCore.LAUNCH_CONFIG_ATTR_RECURSIVE, true)) {
			sb.append("-r ");
		}
		sb.append(configuration.getAttribute(
				AutoTestCore.LAUNCH_CONFIG_ATTR_LOCATION, ""));
		return sb.toString();
	}

	@Override
	public IVMRunner getVMRunner(final ILaunchConfiguration configuration,
			String mode) throws CoreException {
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
							getJavaProject(configuration).getProject(), launch);
					refresher.init();
				}
			}
		};
	}

	public static class BackgroundResourceRefresher implements
			IDebugEventSetListener {
		private IProject project;
		private IProcess process;

		public BackgroundResourceRefresher(IProject project, ILaunch launch) {
			this.project = project;
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
								new IResource[] { project.getFolder("logs") },
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
