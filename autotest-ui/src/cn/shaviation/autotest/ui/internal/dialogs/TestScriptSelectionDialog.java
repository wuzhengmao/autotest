package cn.shaviation.autotest.ui.internal.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.jdt.AutoTestProjects;
import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class TestScriptSelectionDialog extends NonJavaResourceSelectionDialog {

	public TestScriptSelectionDialog(Shell shell, IJavaProject javaProject) {
		super(shell, javaProject);
		setTitle("Select Test Script");
		setMessage("Enter test script name prefix or pattern (*, ?, or camel case):");
	}

	@Override
	protected String getFileExtension() {
		return AutoTestCore.TEST_SCRIPT_FILE_EXTENSION;
	}

	@Override
	protected String getResourceName(Object resource) {
		if (resource instanceof IResource) {
			return AutoTestProjects.getTestScriptName((IResource) resource);
		} else if (resource instanceof IJarEntryResource) {
			return AutoTestProjects
					.getTestScriptName((IJarEntryResource) resource);
		} else {
			return null;
		}
	}

	@Override
	protected ImageDescriptor getResourceImageDescriptor() {
		return UIUtils.getImageDescriptor("script.png");
	}
}