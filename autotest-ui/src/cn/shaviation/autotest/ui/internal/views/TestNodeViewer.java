package cn.shaviation.autotest.ui.internal.views;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import cn.shaviation.autotest.core.TestRunSession;
import cn.shaviation.autotest.runner.TestElement;
import cn.shaviation.autotest.runner.TestExecution;
import cn.shaviation.autotest.runner.TestNode;
import cn.shaviation.autotest.runner.TestElement.Status;
import cn.shaviation.autotest.runner.TestElement.Type;
import cn.shaviation.autotest.ui.internal.actions.OpenTestMethodAction;
import cn.shaviation.autotest.ui.internal.actions.OpenTestScriptAction;
import cn.shaviation.autotest.ui.internal.actions.RerunTestAction;
import cn.shaviation.autotest.util.Strings;

public class TestNodeViewer {

	private final class FailuresOnlyFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			return select((TestElement) element);
		}

		public boolean select(TestElement testElement) {
			Status status = testElement.getStatus();
			if (status == Status.FAILURE || status == Status.ERROR) {
				return true;
			}
			return status == Status.RUNNING
					&& testExecution.getStatus() != Status.RUNNING;
		}
	}

	private static class ReverseList<T> extends AbstractList<T> {

		private final List<T> list;

		public ReverseList(List<T> list) {
			this.list = list;
		}

		@Override
		public T get(int index) {
			return list.get(list.size() - index - 1);
		}

		@Override
		public int size() {
			return list.size();
		}
	}

	private class ExpandAllAction extends Action {
		public ExpandAllAction() {
			setText("Expand All");
			setToolTipText("Expand All Nodes");
		}

		public void run() {
			treeViewer.expandAll();
		}
	}

	private final FailuresOnlyFilter failuresOnlyFilter = new FailuresOnlyFilter();
	private final TestExecutionViewPart testExecutionView;
	private TreeViewer treeViewer;
	private TestExecutionTreeContentProvider treeContentProvider;
	private TestExecutionTreeLabelProvider treeLabelProvider;
	private boolean treeHasFilter = false;
	private TestRunSession session;
	private TestExecution testExecution;
	private boolean treeNeedsRefresh;
	private HashSet<TestElement> needUpdate;
	private TestElement autoScrollTarget;
	private LinkedList<TestNode> autoClose;
	private HashSet<TestNode> autoExpand;

	public TestNodeViewer(Composite parent,
			TestExecutionViewPart testExecutionView) {
		this.testExecutionView = testExecutionView;
		createTestViewers(parent);
		registerViewersRefresh();
		initContextMenu();
	}

	private void createTestViewers(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.SINGLE | SWT.V_SCROLL);
		treeViewer.setUseHashlookup(true);
		treeContentProvider = new TestExecutionTreeContentProvider();
		treeViewer.setContentProvider(treeContentProvider);
		treeLabelProvider = new TestExecutionTreeLabelProvider(
				testExecutionView);
		treeViewer.setLabelProvider(treeLabelProvider);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelected();
			}
		});
		SelectionListener testOpenListener = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handleDefaultSelected();
			}
		};
		treeViewer.getTree().addSelectionListener(testOpenListener);
	}

	private void initContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				handleMenuAboutToShow(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
		treeViewer.getTree().setMenu(menu);
	}

	private String getInvokeTarget(TestElement testElement) {
		String name = testElement.getName();
		if (name.endsWith(")")) {
			int i = name.lastIndexOf('(');
			if (i >= 0) {
				return name.substring(i + 1, name.length() - 1).trim();
			}
		}
		int i = name.indexOf(':');
		if (i > 0) {
			return name.substring(i + 1).trim();
		}
		return name.trim();
	}

	void handleMenuAboutToShow(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();
		if (!selection.isEmpty()) {
			TestElement testElement = (TestElement) selection.getFirstElement();
			switch (testElement.getType()) {
			case ROOT:
				manager.add(new Action("&Run") {
					@Override
					public void run() {
						testExecutionView.rerunTest("run");
					}
				});
				manager.add(new Action("&Debug") {
					@Override
					public void run() {
						testExecutionView.rerunTest("debug");
					}
				});
				manager.add(new Separator());
				manager.add(new ExpandAllAction());
				break;
			case SCRIPT:
				String testScript = getInvokeTarget(testElement);
				manager.add(new OpenTestScriptAction(testExecutionView
						.getViewSite().getShell(), testExecutionView
						.getLaunchedProject(), testScript));
				manager.add(new Separator());
				if (session != null) {
					manager.add(new RerunTestAction("&Run", testExecutionView
							.getViewSite().getShell(), session.getLaunch(),
							testScript, false, "run"));
					manager.add(new RerunTestAction("&Debug", testExecutionView
							.getViewSite().getShell(), session.getLaunch(),
							testScript, false, "debug"));
					manager.add(new Separator());
				} else {
					String logPath = testExecution.getArgs().get(
							TestExecution.ARG_LOG_PATH);
					if (!Strings.isBlank(logPath)) {
						logPath = "file:" + logPath;
					}
					String picPath = testExecution.getArgs().get(
							TestExecution.ARG_PIC_PATH);
					if (!Strings.isBlank(picPath)) {
						logPath = "file:" + picPath;
					}
					manager.add(new RerunTestAction("&Run", testExecutionView
							.getViewSite().getShell(), testExecutionView
							.getLaunchedProject(), testScript, false, logPath,
							picPath, "run"));
					manager.add(new RerunTestAction("&Debug", testExecutionView
							.getViewSite().getShell(), testExecutionView
							.getLaunchedProject(), testScript, false, logPath,
							picPath, "debug"));
					manager.add(new Separator());
				}
				manager.add(new ExpandAllAction());
				break;
			case METHOD:
				String testMethod = getInvokeTarget(testElement);
				manager.add(new OpenTestMethodAction(testExecutionView
						.getViewSite().getShell(), testExecutionView
						.getLaunchedProject(), testMethod));
				manager.add(new Separator());
				manager.add(new ExpandAllAction());
				break;
			default:
				manager.add(new ExpandAllAction());
				break;
			}
		}
		manager.add(new Separator("additions"));
		manager.add(new Separator("additions-end"));
	}

	public void registerActiveTestRunSession(TestRunSession session) {
		this.session = session;
	}

	public void registerActiveTestExecution(TestExecution testExecution) {
		this.testExecution = testExecution;
		registerAutoScrollTarget(null);
		registerViewersRefresh();
	}

	void handleDefaultSelected() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();
		if (!selection.isEmpty()) {
			TestElement testElement = (TestElement) selection.getFirstElement();
			Action action = null;
			switch (testElement.getType()) {
			case SCRIPT:
				String testScript = getInvokeTarget(testElement);
				action = new OpenTestScriptAction(testExecutionView
						.getViewSite().getShell(),
						testExecutionView.getLaunchedProject(), testScript);
				break;
			case METHOD:
				String testMethod = getInvokeTarget(testElement);
				action = new OpenTestMethodAction(testExecutionView
						.getViewSite().getShell(),
						testExecutionView.getLaunchedProject(), testMethod);
				break;
			}
			if (action != null && action.isEnabled()) {
				action.run();
			}
		}
	}

	private void handleSelected() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();
		if (!selection.isEmpty()) {
			TestElement testElement = (TestElement) selection.getFirstElement();
			testExecutionView.handleTestSelected(testElement);
		}
	}

	public synchronized void setShowTime(boolean showTime) {
		try {
			treeViewer.getTree().setRedraw(false);
			treeLabelProvider.setShowTime(showTime);
		} finally {
			treeViewer.getTree().setRedraw(true);
		}
	}

	public synchronized void setShowFailuresOnly(boolean failuresOnly) {
		try {
			treeViewer.getTree().setRedraw(false);
			if (failuresOnly) {
				if (!treeHasFilter) {
					treeNeedsRefresh = true;
					treeHasFilter = true;
					treeViewer.setInput(null);
					treeViewer.addFilter(failuresOnlyFilter);
				}
			} else if (treeHasFilter) {
				treeNeedsRefresh = true;
				treeHasFilter = false;
				treeViewer.setInput(null);
				treeViewer.removeFilter(failuresOnlyFilter);
			}
			processChangesInUI();
		} finally {
			treeViewer.getTree().setRedraw(true);
		}
	}

	public void processChangesInUI() {
		if (testExecution == null) {
			registerViewersRefresh();
			treeNeedsRefresh = false;
			treeViewer.setInput(null);
			return;
		}
		if (treeNeedsRefresh) {
			clearUpdateAndExpansion();
			treeNeedsRefresh = false;
			treeViewer.setInput(testExecution);
		} else {
			Object[] toUpdate;
			synchronized (this) {
				toUpdate = needUpdate.toArray();
				needUpdate.clear();
			}
			if (!treeNeedsRefresh && toUpdate.length > 0) {
				if (treeHasFilter) {
					for (int i = 0; i < toUpdate.length; i++) {
						updateElementInTree((TestElement) toUpdate[i]);
					}
				} else {
					HashSet<Object> toUpdateWithParents = new HashSet<Object>();
					toUpdateWithParents.addAll(Arrays.asList(toUpdate));
					for (int i = 0; i < toUpdate.length; i++) {
						TestNode parent = ((TestElement) toUpdate[i])
								.getParent();
						while (parent != null) {
							toUpdateWithParents.add(parent);
							parent = parent.getParent();
						}
					}
					treeViewer.update(toUpdateWithParents.toArray(), null);
				}
			}
		}
		autoScrollInUI();
	}

	private void updateElementInTree(TestElement element) {
		if (isShown(element)) {
			updateShownElementInTree(element);
		} else {
			TestElement current = element;
			do {
				if (treeViewer.testFindItem(current) != null) {
					treeViewer.remove(current);
				}
				current = current.getParent();
				if (current.getType() == Type.ROOT) {
					break;
				}
			} while (!isShown(current));
			while (current != null && current.getType() != Type.ROOT) {
				treeViewer.update(current, null);
				current = current.getParent();
			}
		}
	}

	private void updateShownElementInTree(TestElement element) {
		if (element == null || element.getType() == Type.ROOT) {
			return;
		}
		TestNode parent = element.getParent();
		updateShownElementInTree(parent);
		if (treeViewer.testFindItem(element) == null) {
			treeViewer.add(parent, element);
		} else {
			treeViewer.update(element, null);
		}
	}

	private boolean isShown(TestElement element) {
		return failuresOnlyFilter.select(element);
	}

	private void autoScrollInUI() {
		if (!testExecutionView.isAutoScroll()) {
			clearAutoExpand();
			autoClose.clear();
			return;
		}
		synchronized (this) {
			for (TestNode node : autoExpand) {
				treeViewer.setExpandedState(node, true);
			}
			clearAutoExpand();
		}
		TestElement current = autoScrollTarget;
		autoScrollTarget = null;
		TestNode parent = current == null ? null
				: (TestNode) treeContentProvider.getParent(current);
		if (autoClose.isEmpty() || !autoClose.getLast().equals(parent)) {
			for (ListIterator<TestNode> iter = autoClose.listIterator(autoClose
					.size()); iter.hasPrevious();) {
				TestNode previousAutoOpened = iter.previous();
				if (previousAutoOpened.equals(parent)) {
					break;
				}
				if (previousAutoOpened.getStatus() == Status.PASS) {
					iter.remove();
					treeViewer.collapseToLevel(previousAutoOpened, -1);
				}
			}
			while (parent != null && !treeViewer.getInput().equals(parent)
					&& !treeViewer.getExpandedState(parent)) {
				autoClose.add(parent);
				parent = (TestNode) treeContentProvider.getParent(parent);
			}
		}
		if (current != null) {
			treeViewer.reveal(current);
		}
	}

	public void selectFirstFailure() {
		TestElement firstFailure = getNextChildFailure(
				(TestExecution) treeViewer.getInput(), true);
		if (firstFailure != null) {
			treeViewer
					.setSelection(new StructuredSelection(firstFailure), true);
		}
	}

	public void selectFailure(boolean showNext) {
		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();
		TestElement selected = (TestElement) selection.getFirstElement();
		TestElement next;
		if (selected == null) {
			next = getNextChildFailure((TestExecution) treeViewer.getInput(),
					showNext);
		} else {
			next = getNextFailure(selected, showNext);
		}
		if (next != null) {
			treeViewer.setSelection(new StructuredSelection(next), true);
		}
	}

	private TestElement getNextFailure(TestElement selected, boolean showNext) {
		if (treeContentProvider.hasChildren(selected)) {
			TestElement nextChild = getNextChildFailure(selected, showNext);
			if (nextChild != null) {
				return nextChild;
			}
		}
		return getNextFailureSibling(selected, showNext);
	}

	private TestElement getNextFailureSibling(TestElement current,
			boolean showNext) {
		TestNode parent = current.getParent();
		if (parent == null) {
			return null;
		}
		List<Object> siblings = Arrays.asList(treeContentProvider
				.getChildren(parent));
		if (!showNext) {
			siblings = new ReverseList<Object>(siblings);
		}
		int nextIndex = siblings.indexOf(current) + 1;
		for (int i = nextIndex; i < siblings.size(); i++) {
			TestElement sibling = (TestElement) siblings.get(i);
			Status status = sibling.getStatus();
			if (status == Status.ERROR || status == Status.FAILURE) {
				if (!treeContentProvider.hasChildren(sibling)) {
					return sibling;
				}
				return getNextChildFailure(sibling, showNext);
			}
		}
		return getNextFailureSibling(parent, showNext);
	}

	private TestElement getNextChildFailure(TestElement root, boolean showNext) {
		List<Object> children = Arrays.asList(treeContentProvider
				.getChildren(root));
		if (!showNext) {
			children = new ReverseList<Object>(children);
		}
		for (int i = 0; i < children.size(); i++) {
			TestElement child = (TestElement) children.get(i);
			Status status = child.getStatus();
			if (status == Status.ERROR || status == Status.FAILURE) {
				if (!treeContentProvider.hasChildren(child)) {
					return child;
				}
				return getNextChildFailure(child, showNext);
			}
		}
		return null;
	}

	public Control getControl() {
		return treeViewer.getTree();
	}

	public synchronized void registerViewersRefresh() {
		treeNeedsRefresh = true;
		clearUpdateAndExpansion();
	}

	private void clearUpdateAndExpansion() {
		needUpdate = new LinkedHashSet<TestElement>();
		autoClose = new LinkedList<TestNode>();
		autoExpand = new HashSet<TestNode>();
	}

	public synchronized void registerTestAdded(TestElement element) {
		treeNeedsRefresh = true;
	}

	public synchronized void registerTestUpdate(TestElement element) {
		needUpdate.add(element);
	}

	private synchronized void clearAutoExpand() {
		autoExpand.clear();
	}

	public void registerAutoScrollTarget(TestElement element) {
		autoScrollTarget = element;
	}

	public synchronized void registerFailedForAutoScroll(TestElement element) {
		Object parent = treeContentProvider.getParent(element);
		if (parent != null) {
			autoExpand.add((TestNode) parent);
		}
	}

	public void expandFirstLevel() {
		treeViewer.collapseAll();
		treeViewer.expandToLevel(2);
		TestExecution execution = (TestExecution) treeViewer.getInput();
		if (execution != null && execution.total() > 0) {
			treeViewer.setSelection(new StructuredSelection(execution
					.getChildren().get(0)), true);
		}
	}
}
