package cn.shaviation.autotest.ui.internal.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.ui.internal.dialogs.TestScriptSelectionDialog;
import cn.shaviation.autotest.ui.internal.util.ControlAccessibleListener;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.util.Logs;
import cn.shaviation.autotest.util.Objects;

public class AutoTestMainTab extends AbstractLaunchConfigurationTab {

	private Text projectText;
	private Button projectButton;
	private Text resourceText;
	private Button resourceButton;
	private Button recursiveCheckButton;
	private WidgetListener widgetListener = new WidgetListener();

	private class WidgetListener implements ModifyListener, SelectionListener {

		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == projectButton) {
				handleProjectButtonSelected();
			} else if (source == resourceButton) {
				handleResourceButtonSelected();
			} else {
				updateLaunchConfigurationDialog();
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = UIUtils.createComposite(parent, parent.getFont(), 1,
				1, GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).verticalSpacing = 0;
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);
		createTestResourceEditor(comp);
		setControl(comp);
	}

	private void createProjectEditor(Composite parent) {
		Group group = UIUtils.createGroup(parent, "&Project:", 2, 1,
				GridData.FILL_HORIZONTAL);
		projectText = UIUtils.createSingleText(group, 1);
		projectText.addModifyListener(widgetListener);
		ControlAccessibleListener.addListener(projectText, group.getText());
		projectButton = createPushButton(group, "&Browse...", null);
		projectButton.addSelectionListener(widgetListener);
	}

	private void handleProjectButtonSelected() {
		IJavaProject javaProject = chooseJavaProject();
		if (javaProject == null) {
			return;
		}
		String projectName = javaProject.getElementName();
		projectText.setText(projectName);
	}

	private IJavaProject chooseJavaProject() {
		ILabelProvider labelProvider = new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), labelProvider);
		dialog.setTitle("Project Selection");
		dialog.setMessage("Select a project to constrain your search.");
		try {
			dialog.setElements(JavaCore.create(getWorkspaceRoot())
					.getJavaProjects());
		} catch (JavaModelException jme) {
			Logs.e(jme);
		}
		IJavaProject javaProject = getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == Window.OK) {
			return (IJavaProject) dialog.getFirstResult();
		}
		return null;
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	protected IJavaProject getJavaProject() {
		String projectName = projectText.getText().trim();
		return !projectName.isEmpty() ? getJavaModel().getJavaProject(
				projectName) : null;
	}

	private void createTestResourceEditor(Composite parent) {
		Group group = UIUtils.createGroup(parent, "&Test resource:", 2, 1,
				GridData.FILL_HORIZONTAL);
		resourceText = UIUtils.createSingleText(group, 1);
		resourceText.addModifyListener(widgetListener);
		ControlAccessibleListener.addListener(resourceText, group.getText());
		resourceButton = createPushButton(group, "&Select...", null);
		resourceButton.addSelectionListener(widgetListener);
		recursiveCheckButton = UIUtils.createCheckButton(group,
				"&Recursively include sub-packages", null, true, 2);
		recursiveCheckButton.addSelectionListener(widgetListener);
	}

	private void handleResourceButtonSelected() {
		IJavaProject javaProject = getJavaProject();
		if (javaProject == null) {
			return;
		}
		TestScriptSelectionDialog dialog = new TestScriptSelectionDialog(
				getShell(), javaProject);
		dialog.setInitialPattern(resourceText.getText().trim());
		if (dialog.open() == Window.OK) {
			resourceText.setText(Objects.toString(dialog.getResult()[0]));
		}
	}

	@Override
	public String getName() {
		return "Main";
	}

	@Override
	public Image getImage() {
		return UIUtils.getImage("script.png");
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				projectText.getText().trim());
		config.setAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_LOCATION,
				resourceText.getText().trim());
		config.setAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_RECURSIVE,
				recursiveCheckButton.getSelection());
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		updateTestResourceFromConfig(config);
		updateRecursiveFromConfig(config);
	}

	private void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = "";
		try {
			projectName = config.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		projectText.setText(projectName);
	}

	private void updateTestResourceFromConfig(ILaunchConfiguration config) {
		String location = "";
		try {
			location = config.getAttribute(
					AutoTestCore.LAUNCH_CONFIG_ATTR_LOCATION, "");
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		resourceText.setText(location);
	}

	private void updateRecursiveFromConfig(ILaunchConfiguration config) {
		boolean recursive = true;
		try {
			recursive = config.getAttribute(
					AutoTestCore.LAUNCH_CONFIG_ATTR_RECURSIVE, true);
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		recursiveCheckButton.setSelection(recursive);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IResource resource = LaunchHelper.getContext();
		if (resource != null) {
			initializeJavaProject(resource, config);
			initializeTestResource(resource, config);
		} else {
			config.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			config.setAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_LOCATION, "");
		}
		config.setAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_RECURSIVE, true);
	}

	private void initializeJavaProject(IResource resource,
			ILaunchConfigurationWorkingCopy config) {
		IProject project = resource.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getElementName();
		}
		config.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	}

	private void initializeTestResource(IResource resource,
			ILaunchConfigurationWorkingCopy config) {
		String location = LaunchHelper.getResourceLocation(resource);
		config.setAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_LOCATION, location);
	}
}
