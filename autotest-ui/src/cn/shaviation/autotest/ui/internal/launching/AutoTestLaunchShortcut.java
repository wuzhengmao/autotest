package cn.shaviation.autotest.ui.internal.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

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
				try {
					LaunchHelper.launch(project, location,
							!(resource instanceof IFile), mode);
				} catch (CoreException e) {
					Logs.e(e);
				}
			}
		}
	}
}
