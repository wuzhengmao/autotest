package cn.shaviation.autotest.ui.internal.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class DocumentEditorContributor extends
		MultiPageEditorActionBarContributor {

	protected DocumentSourcePage<?> activeEditorPart;

	private Map<String, IAction> backupActions = new HashMap<String, IAction>();

	@Override
	public void setActivePage(IEditorPart part) {
		if (activeEditorPart == part) {
			return;
		}
		if (part instanceof DocumentSourcePage) {
			activeEditorPart = (DocumentSourcePage<?>) part;
			IActionBars actionBars = getActionBars();
			if (actionBars != null) {
				backupActions.put(ActionFactory.DELETE.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.DELETE.getId()));
				backupActions.put(ActionFactory.UNDO.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.UNDO.getId()));
				backupActions.put(ActionFactory.REDO.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.REDO.getId()));
				backupActions.put(ActionFactory.CUT.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.CUT.getId()));
				backupActions.put(ActionFactory.COPY.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.COPY.getId()));
				backupActions.put(ActionFactory.PASTE.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.PASTE.getId()));
				backupActions.put(ActionFactory.SELECT_ALL.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.SELECT_ALL
								.getId()));
				backupActions.put(ActionFactory.FIND.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.FIND.getId()));
				backupActions.put(IDEActionFactory.BOOKMARK.getId(), actionBars
						.getGlobalActionHandler(IDEActionFactory.BOOKMARK
								.getId()));
				backupActions.put(ActionFactory.REFRESH.getId(), actionBars
						.getGlobalActionHandler(ActionFactory.REFRESH.getId()));
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
				actionBars
						.setGlobalActionHandler(
								ActionFactory.SELECT_ALL.getId(),
								getAction(editor,
										ITextEditorActionConstants.SELECT_ALL));
				actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
						getAction(editor, ITextEditorActionConstants.FIND));
				actionBars.setGlobalActionHandler(
						IDEActionFactory.BOOKMARK.getId(),
						getAction(editor, IDEActionFactory.BOOKMARK.getId()));
				actionBars.setGlobalActionHandler(
						ActionFactory.REFRESH.getId(),
						getAction(editor, ITextEditorActionConstants.REFRESH));
				actionBars.updateActionBars();
			}
		} else {
			activeEditorPart = null;
			IActionBars actionBars = getActionBars();
			if (actionBars != null && !backupActions.isEmpty()) {
				actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
						backupActions.get(ActionFactory.DELETE.getId()));
				actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
						backupActions.get(ActionFactory.UNDO.getId()));
				actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
						backupActions.get(ActionFactory.REDO.getId()));
				actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
						backupActions.get(ActionFactory.CUT.getId()));
				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
						backupActions.get(ActionFactory.COPY.getId()));
				actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
						backupActions.get(ActionFactory.PASTE.getId()));
				actionBars.setGlobalActionHandler(
						ActionFactory.SELECT_ALL.getId(),
						backupActions.get(ActionFactory.SELECT_ALL.getId()));
				actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
						backupActions.get(ActionFactory.FIND.getId()));
				actionBars.setGlobalActionHandler(
						IDEActionFactory.BOOKMARK.getId(),
						backupActions.get(IDEActionFactory.BOOKMARK.getId()));
				actionBars.setGlobalActionHandler(
						ActionFactory.REFRESH.getId(),
						backupActions.get(ActionFactory.REFRESH.getId()));
				actionBars.updateActionBars();
			}
		}
	}

	private IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}
}
