package cn.shaviation.autotest.ui.internal.views;

import java.util.AbstractList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import cn.shavation.autotest.runner.TestElement;
import cn.shavation.autotest.runner.TestElement.Status;
import cn.shavation.autotest.runner.TestNode;
import cn.shaviation.autotest.ui.internal.views.TestExecutionTreeContentProvider.TreeNode;

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
			return status == Status.RUNNING && !testRunSession.isRunning();
		}
	}

	private static class ReverseList extends AbstractList {

		private final List list;

		public ReverseList(List list) {
			this.list = list;
		}

		@Override
		public Object get(int index) {
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
	private final Clipboard clipboard;
	private TreeViewer treeViewer;
	private ITreeContentProvider treeContentProvider;
	private IBaseLabelProvider treeLabelProvider;
	private SelectionProviderMediator selectionProvider;
	private boolean treeHasFilter;
	private TestRunSession testRunSession;
	private boolean treeNeedsRefresh;
	private HashSet needUpdate;
	private TestCaseElement fAutoScrollTarget;
	private LinkedList autoClose;
	private HashSet autoExpand;

	public TestNodeViewer(Composite parent, Clipboard clipboard,
			TestExecutionViewPart testExecutionView) {
		this.testExecutionView = testExecutionView;
		this.clipboard = clipboard;
		createTestViewers(parent);
		registerViewersRefresh();
		initContextMenu();
	}

	private void createTestViewers(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.SINGLE | SWT.V_SCROLL);
		treeViewer.setUseHashlookup(true);
		treeContentProvider = new TestExecutionTreeContentProvider();
		treeViewer.setContentProvider(treeContentProvider);
		treeLabelProvider = new TestExecutionTreeLabelProvider(testExecutionView);
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

	void handleMenuAboutToShow(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();
		if (!selection.isEmpty()) {
			TestElement testElement = (TestElement) ((TreeNode)selection.getFirstElement()).getElement();
			switch(testElement.getType()) {
			case ROOT:
				break;
			case SCRIPT:
				break;
			}

			String testLabel = testElement.getTestName();
			String className = testElement.getClassName();
			if ((testElement instanceof TestSuiteElement)) {
				manager.add(new OpenTestAction(this.fTestRunnerPart, testLabel));
				manager.add(new Separator());
				if ((testClassExists(className))
						&& (!this.fTestRunnerPart.lastLaunchIsKeptAlive())) {
					manager.add(new RerunAction(
							JUnitMessages.RerunAction_label_run,
							this.fTestRunnerPart, testElement.getId(),
							className, null, "run"));
					manager.add(new RerunAction(
							JUnitMessages.RerunAction_label_debug,
							this.fTestRunnerPart, testElement.getId(),
							className, null, "debug"));
				}
			} else {
				TestCaseElement testCaseElement = (TestCaseElement) testElement;
				String testMethodName = testCaseElement.getTestMethodName();
				manager.add(new OpenTestAction(this.fTestRunnerPart,
						testCaseElement));
				manager.add(new Separator());
				if (this.fTestRunnerPart.lastLaunchIsKeptAlive()) {
					manager.add(new RerunAction(
							JUnitMessages.RerunAction_label_rerun,
							this.fTestRunnerPart, testElement.getId(),
							className, testMethodName, "run"));
				} else {
					manager.add(new RerunAction(
							JUnitMessages.RerunAction_label_run,
							this.fTestRunnerPart, testElement.getId(),
							className, testMethodName, "run"));
					manager.add(new RerunAction(
							JUnitMessages.RerunAction_label_debug,
							this.fTestRunnerPart, testElement.getId(),
							className, testMethodName, "debug"));
				}
			}
			if (this.fLayoutMode == 1) {
				manager.add(new Separator());
				manager.add(new ExpandAllAction());
			}
		}
		if ((this.fTestRunSession != null)
				&& (this.fTestRunSession.getFailureCount()
						+ this.fTestRunSession.getErrorCount() > 0)) {
			if (this.fLayoutMode != 1) {
				manager.add(new Separator());
			}
			manager.add(new CopyFailureListAction(this.fTestRunnerPart,
					this.fClipboard));
		}
		manager.add(new Separator("additions"));
		manager.add(new Separator("additions-end"));
	}

	private boolean testClassExists(String className) {
		IJavaProject project = this.fTestRunnerPart.getLaunchedProject();
		if (project == null) {
			return false;
		}
		try {
			IType type = project.findType(className);
			return type != null;
		} catch (JavaModelException localJavaModelException) {
		}
		return false;
	}

	public Control getTestViewerControl() {
		return this.fViewerbook;
	}

	public synchronized void registerActiveSession(TestRunSession testRunSession) {
		this.fTestRunSession = testRunSession;
		registerAutoScrollTarget(null);
		registerViewersRefresh();
	}

	void handleDefaultSelected() {
		IStructuredSelection selection = (IStructuredSelection) this.fSelectionProvider
				.getSelection();
		if (selection.size() != 1) {
			return;
		}
		TestElement testElement = (TestElement) selection.getFirstElement();
		OpenTestAction action;
		if ((testElement instanceof TestSuiteElement)) {
			action = new OpenTestAction(this.fTestRunnerPart,
					testElement.getTestName());
		} else {
			OpenTestAction action;
			if ((testElement instanceof TestCaseElement)) {
				TestCaseElement testCase = (TestCaseElement) testElement;
				action = new OpenTestAction(this.fTestRunnerPart, testCase);
			} else {
				throw new IllegalStateException(String.valueOf(testElement));
			}
		}
		OpenTestAction action;
		if (action.isEnabled()) {
			action.run();
		}
	}

	private void handleSelected() {
		IStructuredSelection selection = (IStructuredSelection) this.fSelectionProvider
				.getSelection();
		TestElement testElement = null;
		if (selection.size() == 1) {
			testElement = (TestElement) selection.getFirstElement();
		}
		this.fTestRunnerPart.handleTestSelected(testElement);
	}

	public synchronized void setShowTime(boolean showTime) {
		try {
			this.fViewerbook.setRedraw(false);
			this.fTreeLabelProvider.setShowTime(showTime);
			this.fTableLabelProvider.setShowTime(showTime);
		} finally {
			this.fViewerbook.setRedraw(true);
		}
	}

	public synchronized void setShowFailuresOnly(boolean failuresOnly,
			int layoutMode) {
		try {
			this.fViewerbook.setRedraw(false);

			IStructuredSelection selection = null;
			boolean switchLayout = layoutMode != this.fLayoutMode;
			if (switchLayout) {
				selection = (IStructuredSelection) this.fSelectionProvider
						.getSelection();
				if (layoutMode == 1) {
					if (this.fTreeNeedsRefresh) {
						clearUpdateAndExpansion();
					}
				} else if (this.fTableNeedsRefresh) {
					clearUpdateAndExpansion();
				}
				this.fLayoutMode = layoutMode;
				this.fViewerbook.showPage(getActiveViewer().getControl());
			}
			StructuredViewer viewer = getActiveViewer();
			if (failuresOnly) {
				if (!getActiveViewerHasFilter()) {
					setActiveViewerNeedsRefresh(true);
					setActiveViewerHasFilter(true);
					viewer.setInput(null);
					viewer.addFilter(this.fFailuresOnlyFilter);
				}
			} else if (getActiveViewerHasFilter()) {
				setActiveViewerNeedsRefresh(true);
				setActiveViewerHasFilter(false);
				viewer.setInput(null);
				viewer.removeFilter(this.fFailuresOnlyFilter);
			}
			processChangesInUI();
			if (selection != null) {
				StructuredSelection flatSelection = new StructuredSelection(
						selection.toList());
				this.fSelectionProvider.setSelection(flatSelection, true);
			}
		} finally {
			this.fViewerbook.setRedraw(true);
		}
	}

	private boolean getActiveViewerHasFilter() {
		if (this.fLayoutMode == 1) {
			return this.fTreeHasFilter;
		}
		return this.fTableHasFilter;
	}

	private void setActiveViewerHasFilter(boolean filter) {
		if (this.fLayoutMode == 1) {
			this.fTreeHasFilter = filter;
		} else {
			this.fTableHasFilter = filter;
		}
	}

	private StructuredViewer getActiveViewer() {
		if (this.fLayoutMode == 1) {
			return this.fTreeViewer;
		}
		return this.fTableViewer;
	}

	private boolean getActiveViewerNeedsRefresh() {
		if (this.fLayoutMode == 1) {
			return this.fTreeNeedsRefresh;
		}
		return this.fTableNeedsRefresh;
	}

	private void setActiveViewerNeedsRefresh(boolean needsRefresh) {
		if (this.fLayoutMode == 1) {
			this.fTreeNeedsRefresh = needsRefresh;
		} else {
			this.fTableNeedsRefresh = needsRefresh;
		}
	}

	public void processChangesInUI() {
		if (this.fTestRunSession == null) {
			registerViewersRefresh();
			this.fTreeNeedsRefresh = false;
			this.fTableNeedsRefresh = false;
			this.fTreeViewer.setInput(null);
			this.fTableViewer.setInput(null);
			return;
		}
		TestRoot testRoot = this.fTestRunSession.getTestRoot();

		StructuredViewer viewer = getActiveViewer();
		if (getActiveViewerNeedsRefresh()) {
			clearUpdateAndExpansion();
			setActiveViewerNeedsRefresh(false);
			viewer.setInput(testRoot);
		} else {
			synchronized (this) {
				Object[] toUpdate = this.fNeedUpdate.toArray();
				this.fNeedUpdate.clear();
			}
			Object[] toUpdate;
			if ((!this.fTreeNeedsRefresh) && (toUpdate.length > 0)) {
				if (this.fTreeHasFilter) {
					for (int i = 0; i < toUpdate.length; i++) {
						updateElementInTree((TestElement) toUpdate[i]);
					}
				} else {
					HashSet toUpdateWithParents = new HashSet();
					toUpdateWithParents.addAll(Arrays.asList(toUpdate));
					for (int i = 0; i < toUpdate.length; i++) {
						TestElement parent = ((TestElement) toUpdate[i])
								.getParent();
						while (parent != null) {
							toUpdateWithParents.add(parent);
							parent = parent.getParent();
						}
					}
					this.fTreeViewer
							.update(toUpdateWithParents.toArray(), null);
				}
			}
			if ((!this.fTableNeedsRefresh) && (toUpdate.length > 0)) {
				if (this.fTableHasFilter) {
					for (int i = 0; i < toUpdate.length; i++) {
						updateElementInTable((TestElement) toUpdate[i]);
					}
				} else {
					this.fTableViewer.update(toUpdate, null);
				}
			}
		}
		autoScrollInUI();
	}

	private void updateElementInTree(TestElement testElement) {
		if (isShown(testElement)) {
			updateShownElementInTree(testElement);
		} else {
			TestElement current = testElement;
			do {
				if (this.fTreeViewer.testFindItem(current) != null) {
					this.fTreeViewer.remove(current);
				}
				current = current.getParent();
				if ((current instanceof TestRoot)) {
					break;
				}
			} while (!

			isShown(current));
			while ((current != null) && (!(current instanceof TestRoot))) {
				this.fTreeViewer.update(current, null);
				current = current.getParent();
			}
		}
	}

	private void updateShownElementInTree(TestElement testElement) {
		if ((testElement == null) || ((testElement instanceof TestRoot))) {
			return;
		}
		TestSuiteElement parent = testElement.getParent();
		updateShownElementInTree(parent);
		if (this.fTreeViewer.testFindItem(testElement) == null) {
			this.fTreeViewer.add(parent, testElement);
		} else {
			this.fTreeViewer.update(testElement, null);
		}
	}

	private void updateElementInTable(TestElement element) {
		if (isShown(element)) {
			if (this.fTableViewer.testFindItem(element) == null) {
				TestElement previous = getNextFailure(element, false);
				int insertionIndex = -1;
				if (previous != null) {
					TableItem item = (TableItem) this.fTableViewer
							.testFindItem(previous);
					if (item != null) {
						insertionIndex = this.fTableViewer.getTable().indexOf(
								item);
					}
				}
				this.fTableViewer.insert(element, insertionIndex);
			} else {
				this.fTableViewer.update(element, null);
			}
		} else {
			this.fTableViewer.remove(element);
		}
	}

	private boolean isShown(TestElement current) {
		return this.fFailuresOnlyFilter.select(current);
	}

	private void autoScrollInUI() {
		if (!this.fTestRunnerPart.isAutoScroll()) {
			clearAutoExpand();
			this.fAutoClose.clear();
			return;
		}
		if (this.fLayoutMode == 0) {
			if (this.fAutoScrollTarget != null) {
				this.fTableViewer.reveal(this.fAutoScrollTarget);
			}
			return;
		}
		synchronized (this) {
			for (Iterator iter = this.fAutoExpand.iterator(); iter.hasNext();) {
				TestSuiteElement suite = (TestSuiteElement) iter.next();
				this.fTreeViewer.setExpandedState(suite, true);
			}
			clearAutoExpand();
		}
		TestCaseElement current = this.fAutoScrollTarget;
		this.fAutoScrollTarget = null;

		TestSuiteElement parent = current == null ? null
				: (TestSuiteElement) this.fTreeContentProvider
						.getParent(current);
		if ((this.fAutoClose.isEmpty())
				|| (!this.fAutoClose.getLast().equals(parent))) {
			for (ListIterator iter = this.fAutoClose
					.listIterator(this.fAutoClose.size()); iter.hasPrevious();) {
				TestSuiteElement previousAutoOpened = (TestSuiteElement) iter
						.previous();
				if (previousAutoOpened.equals(parent)) {
					break;
				}
				if (previousAutoOpened.getStatus() == TestElement.Status.OK) {
					iter.remove();
					this.fTreeViewer.collapseToLevel(previousAutoOpened, -1);
				}
			}
			while ((parent != null)
					&& (!this.fTestRunSession.getTestRoot().equals(parent))
					&& (!this.fTreeViewer.getExpandedState(parent))) {
				this.fAutoClose.add(parent);
				parent = (TestSuiteElement) this.fTreeContentProvider
						.getParent(parent);
			}
		}
		if (current != null) {
			this.fTreeViewer.reveal(current);
		}
	}

	public void selectFirstFailure() {
		TestCaseElement firstFailure = getNextChildFailure(
				this.fTestRunSession.getTestRoot(), true);
		if (firstFailure != null) {
			getActiveViewer().setSelection(
					new StructuredSelection(firstFailure), true);
		}
	}

	public void selectFailure(boolean showNext) {
		IStructuredSelection selection = (IStructuredSelection) getActiveViewer()
				.getSelection();
		TestElement selected = (TestElement) selection.getFirstElement();
		TestElement next;
		TestElement next;
		if (selected == null) {
			next = getNextChildFailure(this.fTestRunSession.getTestRoot(),
					showNext);
		} else {
			next = getNextFailure(selected, showNext);
		}
		if (next != null) {
			getActiveViewer().setSelection(new StructuredSelection(next), true);
		}
	}

	private TestElement getNextFailure(TestElement selected, boolean showNext) {
		if ((selected instanceof TestSuiteElement)) {
			TestElement nextChild = getNextChildFailure(
					(TestSuiteElement) selected, showNext);
			if (nextChild != null) {
				return nextChild;
			}
		}
		return getNextFailureSibling(selected, showNext);
	}

	private TestCaseElement getNextFailureSibling(TestElement current,
			boolean showNext) {
		TestSuiteElement parent = current.getParent();
		if (parent == null) {
			return null;
		}
		List siblings = Arrays.asList(parent.getChildren());
		if (!showNext) {
			siblings = new ReverseList(siblings);
		}
		int nextIndex = siblings.indexOf(current) + 1;
		for (int i = nextIndex; i < siblings.size(); i++) {
			TestElement sibling = (TestElement) siblings.get(i);
			if (sibling.getStatus().isErrorOrFailure()) {
				if ((sibling instanceof TestCaseElement)) {
					return (TestCaseElement) sibling;
				}
				return getNextChildFailure((TestSuiteElement) sibling, showNext);
			}
		}
		return getNextFailureSibling(parent, showNext);
	}

	private TestCaseElement getNextChildFailure(TestSuiteElement root,
			boolean showNext) {
		List children = Arrays.asList(root.getChildren());
		if (!showNext) {
			children = new ReverseList(children);
		}
		for (int i = 0; i < children.size(); i++) {
			TestElement child = (TestElement) children.get(i);
			if (child.getStatus().isErrorOrFailure()) {
				if ((child instanceof TestCaseElement)) {
					return (TestCaseElement) child;
				}
				return getNextChildFailure((TestSuiteElement) child, showNext);
			}
		}
		return null;
	}

	public synchronized void registerViewersRefresh() {
		this.fTreeNeedsRefresh = true;
		this.fTableNeedsRefresh = true;
		clearUpdateAndExpansion();
	}

	private void clearUpdateAndExpansion() {
		this.fNeedUpdate = new LinkedHashSet();
		this.fAutoClose = new LinkedList();
		this.fAutoExpand = new HashSet();
	}

	public synchronized void registerTestAdded(TestElement testElement) {
		this.fTreeNeedsRefresh = true;
		this.fTableNeedsRefresh = true;
	}

	public synchronized void registerViewerUpdate(TestElement testElement) {
		this.fNeedUpdate.add(testElement);
	}

	private synchronized void clearAutoExpand() {
		this.fAutoExpand.clear();
	}

	public void registerAutoScrollTarget(TestCaseElement testCaseElement) {
		this.fAutoScrollTarget = testCaseElement;
	}

	public synchronized void registerFailedForAutoScroll(TestElement testElement) {
		Object parent = this.fTreeContentProvider.getParent(testElement);
		if (parent != null) {
			this.fAutoExpand.add(parent);
		}
	}

	public void expandFirstLevel() {
		this.fTreeViewer.expandToLevel(2);
	}
}
