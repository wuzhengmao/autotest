package cn.shaviation.autotest.ui.internal.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import cn.shavation.autotest.runner.TestElement;
import cn.shavation.autotest.runner.TestElement.Status;
import cn.shaviation.autotest.ui.internal.actions.OpenEditorAtLineAction;
import cn.shaviation.autotest.util.Strings;

public class FailureTrace implements IMenuListener {

	private static final int MAX_LABEL_LENGTH = 256;
	private static final String FRAME_PREFIX = "at ";

	private Table table;
	private TestExecutionViewPart testExecutionView;
	private String inputTrace;
	private final Clipboard clipboard;
	private TestElement failure;
	private final FailureTableDisplay failureTableDisplay;

	public FailureTrace(Composite parent, Clipboard clipboard,
			TestExecutionViewPart testExecutionView) {
		Assert.isNotNull(clipboard);
		this.table = new Table(parent, 772);
		this.testExecutionView = testExecutionView;
		this.clipboard = clipboard;
		OpenStrategy handler = new OpenStrategy(table);
		handler.addOpenListener(new IOpenEventListener() {
			@Override
			public void handleOpen(SelectionEvent e) {
				if (table.getSelection().length > 0
						&& failure.getStatus() == Status.ERROR) {
					Action a = createOpenEditorAction(getSelectedText());
					if (a != null) {
						a.run();
					}
				}
			}
		});
		initMenu();
		failureTableDisplay = new FailureTableDisplay(table);
	}

	private void initMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
	}

	@Override
	public void menuAboutToShow(IMenuManager manager) {
		if (table.getSelectionCount() > 0
				&& failure.getStatus() == Status.ERROR) {
			Action a = createOpenEditorAction(getSelectedText());
			if (a != null) {
				manager.add(a);
			}
			manager.add(new FailureTraceCopyAction(this, clipboard));
		}
	}

	public String getTrace() {
		return inputTrace;
	}

	private String getSelectedText() {
		return table.getSelection()[0].getText();
	}

	private Action createOpenEditorAction(String traceLine) {
		try {
			String testName = traceLine;
			testName = testName.substring(testName.indexOf(FRAME_PREFIX));
			testName = testName.substring(FRAME_PREFIX.length(),
					testName.lastIndexOf('(')).trim();
			testName = testName.substring(0, testName.lastIndexOf('.'));
			int innerSeparatorIndex = testName.indexOf('$');
			if (innerSeparatorIndex != -1) {
				testName = testName.substring(0, innerSeparatorIndex);
			}
			String lineNumber = traceLine;
			lineNumber = lineNumber.substring(lineNumber.indexOf(':') + 1,
					lineNumber.lastIndexOf(')'));
			int line = Integer.valueOf(lineNumber).intValue();
			return new OpenEditorAtLineAction(testExecutionView.getViewSite()
					.getShell(), testExecutionView.getLaunchedProject(),
					testName, line);
		} catch (NumberFormatException localNumberFormatException) {
		} catch (IndexOutOfBoundsException localIndexOutOfBoundsException) {
		}
		return null;
	}

	Composite getComposite() {
		return table;
	}

	public void refresh() {
		updateTable(inputTrace);
	}

	public void showFailure(TestElement failure) {
		this.failure = failure;
		String trace = "";
		if (failure != null) {
			trace = failure.getDescription();
		}
		if (this.inputTrace != trace) {
			this.inputTrace = trace;
			updateTable(trace);
		}
	}

	private void updateTable(String trace) {
		if (Strings.isBlank(trace)) {
			clear();
			return;
		}
		trace = trace.trim();
		table.setRedraw(false);
		table.removeAll();
		new TextualTrace(trace).display(failureTableDisplay, MAX_LABEL_LENGTH);
		table.setRedraw(true);
	}

	public void setInformation(String text) {
		clear();
		TableItem tableItem = failureTableDisplay.newTableItem();
		tableItem.setText(text);
	}

	public void clear() {
		table.removeAll();
		inputTrace = null;
	}

	public TestElement getFailedTest() {
		return failure;
	}

	public Shell getShell() {
		return table.getShell();
	}

	public FailureTableDisplay getFailureTableDisplay() {
		return failureTableDisplay;
	}
}