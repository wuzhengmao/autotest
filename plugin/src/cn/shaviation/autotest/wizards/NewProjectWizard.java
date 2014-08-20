package cn.shaviation.autotest.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import cn.shaviation.autotest.AutoTestPlugin;
import cn.shaviation.autotest.natures.AutoTestProjectNature;
import cn.shaviation.autotest.util.ProjectUtils;

public class NewProjectWizard extends Wizard implements INewWizard,
		IExecutableExtension {

	private WizardNewProjectCreationPage wizardPage;
	private IConfigurationElement config;
	private IWorkbench workbench;
	private IProject project;

	public NewProjectWizard() {
		super();
		setWindowTitle("New Automatic Testing Project");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
	}

	@Override
	public void addPages() {
		wizardPage = new WizardNewProjectCreationPage("NewAutoTestProject");
		wizardPage.setTitle("Create an Automatic Testing Project");
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		if (project != null) {
			return true;
		}
		final IProject projectHandle = wizardPage.getProjectHandle();
		if (projectHandle == null) {
			return false;
		}
		URI projectURI = (!wizardPage.useDefaults()) ? wizardPage
				.getLocationURI() : null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace
				.newProjectDescription(projectHandle.getName());
		description.setLocationURI(projectURI);
		ProjectUtils.addNatures(description, "org.eclipse.jdt.core.javanature",
				AutoTestProjectNature.ID);
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws CoreException {
				createProject(description, projectHandle, monitor);
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error",
					realException.getMessage());
			return false;
		}
		project = projectHandle;
		BasicNewProjectResourceWizard.updatePerspective(config);
		BasicNewProjectResourceWizard.selectAndReveal(project,
				workbench.getActiveWorkbenchWindow());
		return true;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		this.config = config;
	}

	private void createProject(IProjectDescription description,
			IProject project, IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		try {
			monitor.beginTask("", 2000);
			project.create(description, new SubProgressMonitor(monitor, 1000));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
					monitor, 1000));
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, AutoTestPlugin.ID,
					IStatus.OK, e.getMessage(), e);
			throw new CoreException(status);
		} finally {
			monitor.done();
		}
	}
}
