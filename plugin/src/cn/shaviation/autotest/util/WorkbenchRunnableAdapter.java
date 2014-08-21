package cn.shaviation.autotest.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;

import cn.shaviation.autotest.AutoTestPlugin;

public class WorkbenchRunnableAdapter implements IRunnableWithProgress,
		IThreadListener {
	private boolean fTransfer = false;
	private IWorkspaceRunnable fWorkspaceRunnable;
	private ISchedulingRule fRule;

	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable) {
		this(runnable, ResourcesPlugin.getWorkspace().getRoot());
	}

	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable,
			ISchedulingRule rule) {
		this.fWorkspaceRunnable = runnable;
		this.fRule = rule;
	}

	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable,
			ISchedulingRule rule, boolean transfer) {
		this.fWorkspaceRunnable = runnable;
		this.fRule = rule;
		this.fTransfer = transfer;
	}

	public ISchedulingRule getSchedulingRule() {
		return this.fRule;
	}

	public void threadChange(Thread thread) {
		if (this.fTransfer) {
			Job.getJobManager().transferRule(this.fRule, thread);
		}
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		try {
			JavaCore.run(this.fWorkspaceRunnable, this.fRule, monitor);
		} catch (OperationCanceledException e) {
			throw new InterruptedException(e.getMessage());
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	public void runAsUserJob(String name, final Object jobFamiliy) {
		Job job = new Job(name) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					WorkbenchRunnableAdapter.this.run(monitor);
				} catch (InvocationTargetException e) {
					Throwable cause = e.getCause();
					if ((cause instanceof CoreException)) {
						return ((CoreException) cause).getStatus();
					}
					return new Status(IStatus.ERROR, AutoTestPlugin.ID,
							cause.getMessage(), cause);
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
				monitor.done();
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return jobFamiliy == family;
			}
		};
		job.setRule(this.fRule);
		job.setUser(true);
		job.schedule();
	}
}