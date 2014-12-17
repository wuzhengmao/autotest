package cn.shaviation.autotest.ui.internal.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.util.Projects;
import cn.shaviation.autotest.ui.AutoTestUI;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.ui.internal.util.WorkbenchRunnableAdapter;

public class NewProjectWizard extends Wizard implements INewWizard,
		IExecutableExtension {

	private IWorkbench fWorkbench;
	private IStructuredSelection fSelection;
	private NewJavaProjectWizardPageOne fFirstPage;
	private NewJavaProjectWizardPageTwo fSecondPage;
	private IConfigurationElement fConfigElement;

	public NewProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(UIUtils
				.getImageDescriptor("newjprj_wiz.png"));
		setDialogSettings(AutoTestUI.getDefault().getDialogSettings());
		setWindowTitle("New Automatic Testing Project");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.fWorkbench = workbench;
		this.fSelection = currentSelection;
	}

	@Override
	public void addPages() {
		this.fFirstPage = new NewJavaProjectWizardPageOne() {

			@Override
			public IClasspathEntry[] getDefaultClasspathEntries() {
				IClasspathEntry[] classpathEntries = super
						.getDefaultClasspathEntries();
				IClasspathEntry[] newClasspathEntries = new IClasspathEntry[classpathEntries.length + 1];
				if (classpathEntries.length > 0) {
					System.arraycopy(classpathEntries, 0, newClasspathEntries,
							0, classpathEntries.length);
				}
				newClasspathEntries[newClasspathEntries.length - 1] = JavaCore
						.newContainerEntry(new Path(AutoTestCore.CONTAINER_ID));
				return newClasspathEntries;
			}

			@Override
			public IClasspathEntry[] getSourceClasspathEntries() {
				IClasspathEntry[] classpathEntries = super
						.getSourceClasspathEntries();
				IClasspathEntry[] newClasspathEntries = new IClasspathEntry[classpathEntries.length + 1];
				if (classpathEntries.length > 0) {
					System.arraycopy(classpathEntries, 0, newClasspathEntries,
							0, classpathEntries.length);
				}
				IPath resourceFolderPath = new Path(getProjectName())
						.makeAbsolute().append(
								AutoTestCore.DEFAULT_RESOURCE_FOLDER);
				newClasspathEntries[classpathEntries.length] = JavaCore
						.newSourceEntry(resourceFolderPath);
				return newClasspathEntries;
			}
		};
		addPage(this.fFirstPage);
		this.fSecondPage = new NewJavaProjectWizardPageTwo(this.fFirstPage);
		addPage(this.fSecondPage);
		this.fFirstPage.init(this.fSelection, getActivePart());
	}

	@Override
	public boolean performFinish() {
		boolean res = doCreationJob();
		if (res) {
			IJavaProject javaProject = getCreatedProject();
			IWorkingSet[] workingSets = this.fFirstPage.getWorkingSets();
			if (workingSets.length > 0) {
				PlatformUI.getWorkbench().getWorkingSetManager()
						.addToWorkingSets(javaProject, workingSets);
			}
			BasicNewProjectResourceWizard
					.updatePerspective(this.fConfigElement);
			selectAndReveal(javaProject.getProject());
		}
		return res;
	}

	protected void finishPage(IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		try {
			monitor.beginTask("Creating...", 5);
			this.fSecondPage.performFinish(new SubProgressMonitor(monitor, 4));
			IJavaProject javaProject = getCreatedProject();
			IProject project = javaProject.getProject();
			monitor.subTask("Create logs folder");
			project.getFolder(AutoTestCore.DEFAULT_LOG_FOLDER).create(true,
					false, new SubProgressMonitor(monitor, 1));
			monitor.subTask("Create snapshot folder");
			project.getFolder(AutoTestCore.DEFAULT_PIC_FOLDER).create(true,
					false, new SubProgressMonitor(monitor, 1));
			monitor.subTask("Configure project");
			project.setDefaultCharset("UTF-8", monitor);
			if (!project.hasNature(AutoTestCore.NATURE_ID)) {
				Projects.addNature(project, AutoTestCore.NATURE_ID,
						new SubProgressMonitor(monitor, 1));
			} else {
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}

	protected void selectAndReveal(IResource newResource) {
		BasicNewResourceWizard.selectAndReveal(newResource,
				this.fWorkbench.getActiveWorkbenchWindow());
	}

	private boolean doCreationJob() {
		IWorkspaceRunnable op = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException,
					OperationCanceledException {
				try {
					finishPage(monitor);
				} catch (InterruptedException e) {
					throw new OperationCanceledException(e.getMessage());
				}
			}
		};
		try {
			ISchedulingRule rule = null;
			Job job = Job.getJobManager().currentJob();
			if (job != null)
				rule = job.getRule();
			IRunnableWithProgress runnable = null;
			if (rule != null)
				runnable = new WorkbenchRunnableAdapter(op, rule, true);
			else
				runnable = new WorkbenchRunnableAdapter(op, getSchedulingRule());
			getContainer().run(canRunForked(), true, runnable);
		} catch (InvocationTargetException e) {
			handleFinishException(getShell(), e);
			return false;
		} catch (InterruptedException localInterruptedException) {
			return false;
		}
		return true;
	}

	protected ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	protected boolean canRunForked() {
		return true;
	}

	private IWorkbenchPart getActivePart() {
		IWorkbenchWindow activeWindow = this.fWorkbench
				.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			IWorkbenchPage activePage = activeWindow.getActivePage();
			if (activePage != null) {
				return activePage.getActivePart();
			}
		}
		return null;
	}

	protected void handleFinishException(Shell shell,
			InvocationTargetException e) {
		UIUtils.showError(
				shell,
				"Error Creating Automatic Testing Project",
				"An error occurred while creating the Automatic Testing project.",
				e);
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		this.fConfigElement = cfig;
	}

	@Override
	public boolean performCancel() {
		this.fSecondPage.performCancel();
		return super.performCancel();
	}

	public IJavaProject getCreatedProject() {
		return this.fSecondPage.getJavaProject();
	}
}