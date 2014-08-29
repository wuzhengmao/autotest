package cn.shaviation.autotest.ui.internal.editors;

import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import cn.shaviation.autotest.core.jdt.AutoTestProjects;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.core.util.Logs;
import cn.shaviation.autotest.ui.internal.dialogs.TestMethodSelectionDialog;

public class DocumentEditorContributor extends
		MultiPageEditorActionBarContributor {

	protected DocumentSourcePage<?> activeEditorPart;

	@Override
	public void setActivePage(IEditorPart part) {
		if (activeEditorPart == part || !(part instanceof DocumentSourcePage)) {
			return;
		}
		activeEditorPart = (DocumentSourcePage<?>) part;
		IActionBars actionBars = getActionBars();
		if (actionBars != null) {
			ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part
					: null;
			actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
					getAction(editor, ITextEditorActionConstants.DELETE));
			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
					getAction(editor, ITextEditorActionConstants.UNDO));
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
					getAction(editor, ITextEditorActionConstants.REDO));
			actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
					getAction(editor, ITextEditorActionConstants.CUT));
			actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
					getAction(editor, ITextEditorActionConstants.COPY));
			actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
					getAction(editor, ITextEditorActionConstants.PASTE));
			actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
					getAction(editor, ITextEditorActionConstants.SELECT_ALL));
			actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
					getAction(editor, ITextEditorActionConstants.FIND));
			actionBars.setGlobalActionHandler(
					IDEActionFactory.BOOKMARK.getId(),
					getAction(editor, IDEActionFactory.BOOKMARK.getId()));
			actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(),
					getAction(editor, ITextEditorActionConstants.REFRESH));
			actionBars.updateActionBars();
		}
	}

	private IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}

	@Override
	public void contributeToToolBar(IToolBarManager manager) {
		Action action = new Action() {
			public void run() {
				test();
			}
		};
		action.setText("Sample Action");
		action.setToolTipText("Sample Action tool tip");
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(IDE.SharedImages.IMG_OBJS_TASK_TSK));
		manager.add(new Separator());
		manager.add(action);
	}

	private void test() {
		IProject project = activeEditorPart.getEditorInput().getFile()
				.getProject();
		IJavaProject javaProject = JavaUtils.getJavaProject(project);
		TestMethodSelectionDialog dialog = new TestMethodSelectionDialog(
				activeEditorPart.getSite().getShell(), javaProject);
		if (dialog.open() == Window.OK) {
			IAnnotation annotation = (IAnnotation) dialog.getResult()[0];
			String testMethod = AutoTestProjects
					.getTestMethodQualifiedName(annotation);
			Logs.i(testMethod);
			if (!AutoTestProjects.checkTestMethod(javaProject, testMethod)) {
				Logs.e("checkTestMethod fail");
			}
		}
		try {
			for (Entry<String, String> entry : AutoTestProjects
					.searchTestDataFiles(javaProject).entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
				if (!entry.getValue().equals(
						AutoTestProjects.getTestDataName(javaProject,
								entry.getKey()))) {
					Logs.e("getTestDataName fail");
				}
				if (!AutoTestProjects.checkNonJavaResource(javaProject,
						entry.getKey())) {
					Logs.e("checkNonJavaResource fail");
				}
			}
		} catch (CoreException e) {
			Logs.e(e);
		}
	}
}
