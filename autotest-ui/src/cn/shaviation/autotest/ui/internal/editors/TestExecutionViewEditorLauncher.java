package cn.shaviation.autotest.ui.internal.editors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

import cn.shaviation.autotest.ui.AutoTestUI;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.ui.internal.views.TestExecutionViewPart;

public class TestExecutionViewEditorLauncher implements IEditorLauncher {

	@Override
	public void open(IPath file) {
		try {
			IViewPart view = AutoTestUI.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.showView(AutoTestUI.TEST_EXECUTION_VIEW_ID);
			if (view instanceof TestExecutionViewPart) {
				((TestExecutionViewPart) view).open(ResourcesPlugin
						.getWorkspace().getRoot().getFileForLocation(file));
			}
		} catch (PartInitException e) {
			UIUtils.showError(AutoTestUI.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					"Open Test Execution",
					"An error occurred while opening a test execution file.", e);
		}
	}
}
