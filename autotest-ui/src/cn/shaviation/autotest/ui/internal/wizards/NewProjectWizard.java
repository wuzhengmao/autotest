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
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.core.util.Projects;
import cn.shaviation.autotest.ui.AutoTestUI;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.ui.internal.util.WorkbenchRunnableAdapter;
import cn.shaviation.autotest.util.Logs;

public class NewProjectWizard extends Wizard implements INewWizard,
		IExecutableExtension {

	private static final String[] ALLOW_PACKAGES;

	private IWorkbench fWorkbench;
	private IStructuredSelection fSelection;
	private NewJavaProjectWizardPageOne fFirstPage;
	private NewJavaProjectWizardPageTwo fSecondPage;
	private IConfigurationElement fConfigElement;

	static {
		ALLOW_PACKAGES = new String[] { "cn/shaviation/autotest/*",
				"cn/shaviation/autotest/annotation/*",
				"cn/shaviation/autotest/model/*" };
	}

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
		this.fFirstPage = new NewJavaProjectWizardPageOne();
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
			monitor.beginTask("Creating...", 4);
			this.fSecondPage.performFinish(new SubProgressMonitor(monitor, 2));
			monitor.subTask("Add autotest-runtime to class path");
			IJavaProject javaProject = getCreatedProject();
			try {
				String location = AutoTest.Plugin.getDefault().getBundle()
						.getLocation();
				int index = location.indexOf("file:/");
				if (index >= 0) {
					List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
					File path = new File(location.substring(index + 6));
					if (path.isDirectory()) {
						classpathEntries.add(createClasspathEntry(new File(
								path, "bin")));
						File libs = new File(path, "lib");
						if (libs.exists() && libs.isDirectory()) {
							for (File lib : libs.listFiles()) {
								if (lib.getName().toLowerCase()
										.endsWith(".jar")) {
									classpathEntries
											.add(createClasspathEntry(lib));
								}
							}
						}
					} else {
						classpathEntries.add(createClasspathEntry(path));
					}
					JavaUtils.addClasspathEntries(javaProject, classpathEntries
							.toArray(new IClasspathEntry[classpathEntries
									.size()]), null);
				}
			} catch (Exception e) {
				Logs.e(e);
			} finally {
				monitor.worked(1);
			}
			IProject project = javaProject.getProject();
			project.setDefaultCharset("UTF-8", monitor);
			if (!project.hasNature(AutoTestCore.NATURE_ID)) {
				Projects.addNature(project, AutoTestCore.NATURE_ID,
						new SubProgressMonitor(monitor, 1));
			}
		} finally {
			monitor.done();
		}
	}

	private IClasspathEntry createClasspathEntry(File path) throws IOException {
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
							null, null, createAccessRules(), null, false);
				}
			}
		}
		return JavaCore.newLibraryEntry(new Path(lib), null, null,
				createAccessRules(), null, false);
	}

	private IAccessRule[] createAccessRules() {
		IAccessRule[] rules = new IAccessRule[ALLOW_PACKAGES.length + 1];
		for (int i = 0; i < ALLOW_PACKAGES.length; i++) {
			rules[i] = JavaCore.newAccessRule(new Path(ALLOW_PACKAGES[i]),
					IAccessRule.K_ACCESSIBLE);
		}
		rules[ALLOW_PACKAGES.length] = JavaCore.newAccessRule(new Path("**/*"),
				IAccessRule.K_NON_ACCESSIBLE);
		return rules;
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