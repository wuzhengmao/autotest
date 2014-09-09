package cn.shaviation.autotest.ui.internal.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IAccessRule;
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

import cn.shavation.autotest.AutoTest;
import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.util.Projects;
import cn.shaviation.autotest.ui.AutoTestUI;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.ui.internal.util.WorkbenchRunnableAdapter;
import cn.shaviation.autotest.util.Logs;

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
				IClasspathEntry[] classpathEntries1 = super
						.getDefaultClasspathEntries();
				IClasspathEntry[] classpathEntries2 = createDependentClasspathEntries();
				IClasspathEntry[] newClasspathEntries = new IClasspathEntry[classpathEntries1.length
						+ classpathEntries2.length];
				if (classpathEntries1.length > 0) {
					System.arraycopy(classpathEntries1, 0, newClasspathEntries,
							0, classpathEntries1.length);
				}
				if (classpathEntries2.length > 0) {
					System.arraycopy(classpathEntries2, 0, newClasspathEntries,
							classpathEntries1.length, classpathEntries2.length);
				}
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
						.makeAbsolute().append("resources");
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
			this.fSecondPage.performFinish(new SubProgressMonitor(monitor, 3));
			IJavaProject javaProject = getCreatedProject();
			IProject project = javaProject.getProject();
			monitor.subTask("Create logs folder");
			project.getFolder("logs").create(true, false,
					new SubProgressMonitor(monitor, 1));
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

	private IClasspathEntry[] createDependentClasspathEntries() {
		List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
		File path;
		try {
			path = FileLocator.getBundleFile(AutoTest.Plugin.getDefault()
					.getBundle());
		} catch (IOException e) {
			Logs.e(e);
			return new IClasspathEntry[0];
		}
		if (path.isDirectory()) {
			try {
				File bin = new File(path, "bin");
				classpathEntries.add(createClasspathEntry(bin.exists() ? bin
						: path, true));
			} catch (IOException e) {
				Logs.e(e);
			}
			File libs = new File(path, "lib");
			if (libs.exists() && libs.isDirectory()) {
				for (File lib : libs.listFiles()) {
					if (lib.getName().toLowerCase().endsWith(".jar")) {
						try {
							classpathEntries.add(createClasspathEntry(lib,
									false));
						} catch (IOException e) {
							Logs.e(e);
						}
					}
				}
			}
		} else {
			try {
				classpathEntries.add(createClasspathEntry(path, true));
			} catch (IOException e) {
				Logs.e(e);
			}
		}
		return classpathEntries.toArray(new IClasspathEntry[classpathEntries
				.size()]);
	}

	private IClasspathEntry createClasspathEntry(File path, boolean primary)
			throws IOException {
		String lib = path.getCanonicalPath();
		for (String name : JavaCore.getClasspathVariableNames()) {
			IPath vp = JavaCore.getClasspathVariable(name);
			if (vp != null && !vp.isEmpty()) {
				String var = vp.toFile().getCanonicalPath();
				if (lib.startsWith(var)) {
					lib = lib.substring(var.length());
					if (!lib.startsWith("/") && !lib.startsWith("\\")) {
						lib = "/" + lib;
					}
					return JavaCore.newVariableEntry(new Path(name + lib),
							null, null, primary ? createAccessRules() : null,
							null, false);
				}
			}
		}
		return JavaCore.newLibraryEntry(new Path(lib), null, null,
				primary ? createAccessRules() : null, null, false);
	}

	private IAccessRule[] createAccessRules() {
		return new IAccessRule[] { JavaCore.newAccessRule(new Path(
				"**/internal/**/*"), IAccessRule.K_NON_ACCESSIBLE) };
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