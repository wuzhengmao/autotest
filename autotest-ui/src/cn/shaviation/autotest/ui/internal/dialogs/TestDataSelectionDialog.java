package cn.shaviation.autotest.ui.internal.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.jdt.AutoTestProjects;
import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class TestDataSelectionDialog extends NonJavaResourceSelectionDialog {

	public TestDataSelectionDialog(Shell shell, IJavaProject javaProject) {
		super(shell, javaProject);
		setTitle("Select Test Data");
		setMessage("Enter test data name prefix or pattern (*, ?, or camel case):");
	}

	@Override
	protected String getFileExtension() {
		return AutoTestCore.TEST_DATA_FILE_EXTENSION;
	}

	@Override
	protected String getResourceName(Object resource) {
		if (resource instanceof IResource) {
			return AutoTestProjects.getTestDataName((IResource) resource);
		} else if (resource instanceof IJarEntryResource) {
			return AutoTestProjects
					.getTestDataName((IJarEntryResource) resource);
		} else {
			return null;
		}
	}

	@Override
	protected ImageDescriptor getResourceImageDescriptor() {
		return UIUtils.getImageDescriptor("testdata.png");
	}
}