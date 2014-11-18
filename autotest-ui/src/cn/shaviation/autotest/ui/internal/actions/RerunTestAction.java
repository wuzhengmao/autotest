package cn.shaviation.autotest.ui.internal.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

import cn.shaviation.autotest.ui.internal.launching.LaunchHelper;
import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class RerunTestAction extends Action {

	private Shell shell;
	private IJavaProject javaProject;
	private String location;
	private boolean recursive;
	private String logPath;
	private String mode;

	public RerunTestAction(String text, Shell shell, IJavaProject javaProject,
			String location, boolean recursive, String logPath, String mode) {
		super(text);
		this.shell = shell;
		this.javaProject = javaProject;
		this.location = location;
		this.recursive = recursive;
		this.logPath = logPath;
		this.mode = mode;
	}

	@Override
	public void run() {
		try {
			LaunchHelper.launch(javaProject.getProject(), location, recursive,
					logPath, mode);
		} catch (CoreException e) {
			UIUtils.showError(shell, "Launch Failed", e.getMessage(), e);
		}
	}
}
