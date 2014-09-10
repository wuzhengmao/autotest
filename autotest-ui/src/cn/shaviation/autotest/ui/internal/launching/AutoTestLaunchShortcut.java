package cn.shaviation.autotest.ui.internal.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.util.Logs;

public class AutoTestLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			launch(((IStructuredSelection) selection).getFirstElement(), mode);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		launch(editor.getEditorInput(), mode);
	}

	private void launch(Object element, String mode) {
		Object resource = LaunchHelper.getResource(element);
		if (resource != null) {
			IProject project = LaunchHelper.getProject(resource);
			String location = LaunchHelper.getResourceLocation(resource);
			if (project != null && location != null) {
				launch(project, location, !(resource instanceof IFile), mode);
			}
		}
	}

	private void launch(IProject project, String location, boolean recursive,
			String mode) {
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType launchConfigType = launchManager
					.getLaunchConfigurationType(AutoTestCore.LAUNCH_CONFIG_TYPE);
			ILaunchConfigurationWorkingCopy workingCopy = launchConfigType
					.newInstance(
							null,
							"Launching automatic testing on "
									+ project.getName() + "-"
									+ location.replace('/', '-'));
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					project.getName());
			workingCopy.setAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_LOCATION,
					location);
			workingCopy.setAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_RECURSIVE,
					true);
			workingCopy.setAttribute(AutoTestCore.LAUNCH_CONFIG_ATTR_LOG_PATH,
					project.getFolder(AutoTestCore.DEFAULT_LOG_FOLDER)
							.getFullPath().toString());
			IPath path = JavaUtils.getJREContainerPath(project);
			if (path != null) {
				workingCopy
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
								path.toPortableString());
			}
			DebugUITools.launch(workingCopy, mode);
		} catch (CoreException e) {
			Logs.e(e);
		}
	}
}
