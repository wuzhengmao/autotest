package cn.shaviation.autotest.ui.internal.views;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.util.Logs;

public abstract class OpenEditorAction extends Action {

	protected String className;
	protected TestExecutionViewPart testExecutionView;
	private final boolean fActivate;

	protected OpenEditorAction(TestExecutionViewPart testExecutionView,
			String className) {
		this(testExecutionView, className, true);
	}

	public OpenEditorAction(TestExecutionViewPart testExecutionView,
			String className, boolean activate) {
		super("&Go to File");
		this.className = className;
		this.testExecutionView = testExecutionView;
		this.fActivate = activate;
	}

	@Override
	public void run() {
		IEditorPart editor = null;
		try {
			IJavaElement element = findElement(getLaunchedProject(), className);
			if (element == null) {
				UIUtils.showError(getShell(), "Cannot Open Editor", "Class \""
						+ className + "\" not found in selected project");
				return;
			}
			editor = JavaUI.openInEditor(element, this.fActivate, false);
		} catch (CoreException e) {
			UIUtils.showError(getShell(), "Error", "Cannot open editor", e);
			return;
		}
		if (editor instanceof ITextEditor) {
			reveal((ITextEditor) editor);
		}
	}

	protected Shell getShell() {
		return testExecutionView.getSite().getShell();
	}

	protected IJavaProject getLaunchedProject() {
		return testExecutionView.getLaunchedProject();
	}

	protected String getClassName() {
		return className;
	}

	protected abstract IJavaElement findElement(IJavaProject paramIJavaProject,
			String paramString) throws CoreException;

	protected abstract void reveal(ITextEditor paramITextEditor);

	protected final IType findType(final IJavaProject project, String className) {
		final IType[] result = new IType[1];
		final String dottedName = className.replace('$', '.');
		try {
			PlatformUI.getWorkbench().getProgressService()
					.busyCursorWhile(new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							try {
								if (project == null) {
									int lastDot = dottedName.lastIndexOf('.');
									TypeNameMatchRequestor nameMatchRequestor = new TypeNameMatchRequestor() {
										@Override
										public void acceptTypeNameMatch(
												TypeNameMatch match) {
											result[0] = match.getType();
										}
									};
									new SearchEngine()
											.searchAllTypeNames(
													lastDot >= 0 ? dottedName
															.substring(0,
																	lastDot)
															.toCharArray()
															: null,
													8,
													(lastDot >= 0 ? dottedName
															.substring(lastDot + 1)
															: dottedName)
															.toCharArray(),
													8,
													0,
													SearchEngine
															.createWorkspaceScope(),
													nameMatchRequestor, 3,
													monitor);
								} else {
									result[0] = internalFindType(project,
											dottedName,
											new HashSet<IJavaProject>(),
											monitor);
								}
							} catch (JavaModelException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
		} catch (InvocationTargetException e) {
			Logs.e(e);
		} catch (InterruptedException localInterruptedException) {
		}
		return result[0];
	}

	private IType internalFindType(IJavaProject project, String className,
			Set<IJavaProject> visitedProjects, IProgressMonitor monitor)
			throws JavaModelException {
		try {
			if (visitedProjects.contains(project)) {
				return null;
			}
			monitor.beginTask("", 2);
			IType type = project.findType(className, new SubProgressMonitor(
					monitor, 1));
			if (type != null) {
				return type;
			}
			visitedProjects.add(project);
			IJavaModel javaModel = project.getJavaModel();
			String[] requiredProjectNames = project.getRequiredProjectNames();
			IProgressMonitor reqMonitor = new SubProgressMonitor(monitor, 1);
			reqMonitor.beginTask("", requiredProjectNames.length);
			for (int i = 0; i < requiredProjectNames.length; i++) {
				IJavaProject requiredProject = javaModel
						.getJavaProject(requiredProjectNames[i]);
				if (requiredProject.exists()) {
					type = internalFindType(requiredProject, className,
							visitedProjects, new SubProgressMonitor(reqMonitor,
									1));
					if (type != null) {
						return type;
					}
				}
			}
			return null;
		} finally {
			monitor.done();
		}
	}
}
