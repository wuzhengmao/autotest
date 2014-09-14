package cn.shaviation.autotest.ui.internal.views;

import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import cn.shavation.autotest.runner.TestElement;
import cn.shavation.autotest.runner.TestExecution;
import cn.shavation.autotest.runner.TestExecutionHelper;
import cn.shaviation.autotest.ui.internal.actions.RerunTestAction;
import cn.shaviation.autotest.ui.internal.util.EmptyLayout;
import cn.shaviation.autotest.ui.internal.util.ImageUtils;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.util.Strings;

public class TestExecutionViewPart extends ViewPart {

	public static final Object FAMILY_AUTOTEST_RUN = new Object();

	private Composite parentComposite;
	private Clipboard clipboard;
	private Composite counterComposite;
	private CounterPanel counterPanel;
	private TestProgressBar progressBar;
	private SashForm sashForm;
	private TestNodeViewer testNodeViewer;
	private FailureTrace failureTrace;
	private Action nextAction;
	private Action prevAction;
	private Action stopAction;
	private Action rerunAction;
	private Action failuresOnlyFilterAction;
	private Action scrollLockAction;
	private Action showTimeAction;
	private Action copyAction;

	private TestExecution testExecution;
	private IMemento memento;
	private boolean disposed = false;
	private int currentOrientation;
	private boolean autoScroll;
	Image scriptIcon;
	Image scriptPassIcon;
	Image scriptErrorIcon;
	Image scriptFailureIcon;
	Image scriptBlockedIcon;
	Image scriptRunningIcon;
	Image methodIcon;
	Image methodPassIcon;
	Image methodErrorIcon;
	Image methodFailureIcon;
	Image methodBlockedIcon;
	Image methodRunningIcon;
	Image loopIcon;
	Image loopPassIcon;
	Image loopErrorIcon;
	Image loopFailureIcon;
	Image loopBlockedIcon;
	Image loopRunningIcon;

	public TestExecutionViewPart() {
		super();
		scriptIcon = UIUtils.getImage("script.png");
		scriptPassIcon = ImageUtils.compositeImage(scriptIcon,
				UIUtils.getImage("success_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		scriptErrorIcon = ImageUtils.compositeImage(scriptIcon,
				UIUtils.getImage("error_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		scriptFailureIcon = ImageUtils.compositeImage(scriptIcon,
				UIUtils.getImage("failed_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		scriptBlockedIcon = ImageUtils.compositeImage(scriptIcon,
				UIUtils.getImage("blocked_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		scriptRunningIcon = ImageUtils.compositeImage(scriptIcon,
				UIUtils.getImage("run_co.gif"), ImageUtils.BOTTOM_LEFT);
		methodIcon = JavaUI.getSharedImages().getImage(
				ISharedImages.IMG_OBJS_CUNIT);
		methodPassIcon = ImageUtils.compositeImage(methodIcon,
				UIUtils.getImage("success_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		methodErrorIcon = ImageUtils.compositeImage(methodIcon,
				UIUtils.getImage("error_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		methodFailureIcon = ImageUtils.compositeImage(methodIcon,
				UIUtils.getImage("failed_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		methodBlockedIcon = ImageUtils.compositeImage(methodIcon,
				UIUtils.getImage("blocked_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		methodRunningIcon = ImageUtils.compositeImage(methodIcon,
				UIUtils.getImage("run_co.gif"), ImageUtils.BOTTOM_LEFT);
		loopIcon = UIUtils.getImage("loop.gif");
		loopPassIcon = ImageUtils.compositeImage(loopIcon,
				UIUtils.getImage("success_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		loopErrorIcon = ImageUtils.compositeImage(loopIcon,
				UIUtils.getImage("error_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		loopFailureIcon = ImageUtils.compositeImage(loopIcon,
				UIUtils.getImage("failed_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		loopBlockedIcon = ImageUtils.compositeImage(loopIcon,
				UIUtils.getImage("blocked_ovr.gif"), ImageUtils.BOTTOM_LEFT);
		loopRunningIcon = ImageUtils.compositeImage(loopIcon,
				UIUtils.getImage("run_co.gif"), ImageUtils.BOTTOM_LEFT);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
		IWorkbenchSiteProgressService progressService = getProgressService();
		if (progressService != null) {
			progressService.showBusyForFamily(FAMILY_AUTOTEST_RUN);
		}
	}

	private IWorkbenchSiteProgressService getProgressService() {
		Object siteService = getSite().getAdapter(
				IWorkbenchSiteProgressService.class);
		if (siteService != null)
			return (IWorkbenchSiteProgressService) siteService;
		return null;
	}

	@Override
	public void createPartControl(Composite parent) {
		parentComposite = parent;
		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Point size = parentComposite.getSize();
				if ((size.x != 0) && (size.y != 0)) {
					if (size.x > size.y) {
						setOrientation(1);
					} else {
						setOrientation(0);
					}
				}
			}
		});
		clipboard = new Clipboard(parent.getDisplay());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);
		configureToolBar();
		counterComposite = createProgressCountPanel(parent);
		counterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sashForm = createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		IActionBars actionBars = getViewSite().getActionBars();
		copyAction = new FailureTraceCopyAction(failureTrace, clipboard);
		copyAction.setActionDefinitionId(ActionFactory.COPY.getCommandId());
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
				copyAction);
		setShowFailuresOnly(false);
		setShowExecutionTime(true);
		if (memento != null) {
			restoreLayoutState(memento);
			memento = null;
		}
	}

	private void configureToolBar() {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		IMenuManager viewMenu = actionBars.getMenuManager();
		nextAction = new Action("Next Failure") {
			@Override
			public void run() {
				testNodeViewer.selectFailure(true);
			}
		};
		nextAction.setDisabledImageDescriptor(UIUtils
				.getImageDescriptor("select_next_disabled.gif"));
		nextAction.setHoverImageDescriptor(UIUtils
				.getImageDescriptor("select_next.gif"));
		nextAction.setImageDescriptor(UIUtils
				.getImageDescriptor("select_next.gif"));
		nextAction.setToolTipText("Next Failed Test Step");
		nextAction.setEnabled(false);
		actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(),
				nextAction);
		prevAction = new Action("Previous Failure") {
			@Override
			public void run() {
				testNodeViewer.selectFailure(false);
			}
		};
		prevAction.setDisabledImageDescriptor(UIUtils
				.getImageDescriptor("select_prev_disabled.gif"));
		prevAction.setHoverImageDescriptor(UIUtils
				.getImageDescriptor("select_prev.gif"));
		prevAction.setImageDescriptor(UIUtils
				.getImageDescriptor("select_prev.gif"));
		prevAction.setToolTipText("Previous Failed Test Step");
		prevAction.setEnabled(false);
		actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(),
				prevAction);
		stopAction = new Action("Stop Test") {
			@Override
			public void run() {
				testNodeViewer.selectFailure(false);
			}
		};
		stopAction.setDisabledImageDescriptor(UIUtils
				.getImageDescriptor("stop_disabled.gif"));
		stopAction.setHoverImageDescriptor(UIUtils
				.getImageDescriptor("stop.gif"));
		stopAction.setImageDescriptor(UIUtils.getImageDescriptor("stop.gif"));
		stopAction.setToolTipText("Stop Test Execution");
		stopAction.setEnabled(false);
		// FIXME
		rerunAction = new RerunTestAction("Rerun Test", getViewSite()
				.getShell(), getLaunchedProject(), "???", true, "run");
		rerunAction.setDisabledImageDescriptor(UIUtils
				.getImageDescriptor("relaunch_disabled.gif"));
		rerunAction.setHoverImageDescriptor(UIUtils
				.getImageDescriptor("relaunch.gif"));
		rerunAction.setImageDescriptor(UIUtils
				.getImageDescriptor("relaunch.gif"));
		rerunAction.setToolTipText("Rerun Last Test Execution");
		rerunAction.setEnabled(false);
		failuresOnlyFilterAction = new Action("", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				setShowFailuresOnly(isChecked());
			}
		};
		failuresOnlyFilterAction.setToolTipText("Show &Failures Only");
		failuresOnlyFilterAction.setImageDescriptor(UIUtils
				.getImageDescriptor("failures.gif"));
		scrollLockAction = new Action("Scroll Lock", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				setAutoScroll(!isChecked());
			}
		};
		scrollLockAction.setImageDescriptor(UIUtils
				.getImageDescriptor("lock.gif"));
		scrollLockAction.setToolTipText("Scroll Lock");
		scrollLockAction.setChecked(!autoScroll);
		showTimeAction = new Action("Show Execution &Time", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				setShowExecutionTime(isChecked());
			}
		};
		toolBar.add(nextAction);
		toolBar.add(prevAction);
		toolBar.add(failuresOnlyFilterAction);
		toolBar.add(scrollLockAction);
		toolBar.add(new Separator());
		toolBar.add(rerunAction);
		toolBar.add(stopAction);
		viewMenu.add(showTimeAction);
		viewMenu.add(new Separator());
		viewMenu.add(failuresOnlyFilterAction);
		actionBars.updateActionBars();
	}

	private Composite createProgressCountPanel(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		setCounterColumns(layout);
		counterPanel = new CounterPanel(composite);
		counterPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		progressBar = new TestProgressBar(composite);
		progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}

	private SashForm createSashForm(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		ViewForm top = new ViewForm(sashForm, SWT.NONE);
		Composite empty = new Composite(top, SWT.NONE);
		empty.setLayout(new EmptyLayout() {
			@Override
			protected Point computeSize(Composite composite, int wHint,
					int hHint, boolean flushCache) {
				return new Point(1, 1);
			}
		});
		top.setTopLeft(empty);
		testNodeViewer = new TestNodeViewer(top, clipboard, this);
		top.setContent(testNodeViewer.getControl());
		ViewForm bottom = new ViewForm(sashForm, SWT.NONE);
		CLabel label = new CLabel(bottom, SWT.NONE);
		label.setText("Failure Trace");
		label.setImage(UIUtils.getImage("stackframe.gif"));
		bottom.setTopLeft(label);
		failureTrace = new FailureTrace(bottom, clipboard, this);
		bottom.setContent(failureTrace.getComposite());
		sashForm.setWeights(new int[] { 50, 50 });
		return sashForm;
	}

	private void setOrientation(int orientation) {
		if (sashForm == null || sashForm.isDisposed()) {
			return;
		}
		sashForm.setOrientation(orientation == 1 ? SWT.HORIZONTAL
				: SWT.VERTICAL);
		currentOrientation = orientation;
		GridLayout layout = (GridLayout) counterComposite.getLayout();
		setCounterColumns(layout);
		parentComposite.layout();
	}

	private void setCounterColumns(GridLayout layout) {
		if (currentOrientation == 1) {
			layout.numColumns = 2;
		} else {
			layout.numColumns = 1;
		}
	}

	private void setShowFailuresOnly(boolean failuresOnly) {
		failuresOnlyFilterAction.setChecked(failuresOnly);
		testNodeViewer.setShowFailuresOnly(failuresOnly);
	}

	private void setShowExecutionTime(boolean showTime) {
		showTimeAction.setChecked(showTime);
		testNodeViewer.setShowTime(showTime);
	}

	private void restoreLayoutState(IMemento memento) {
		Integer ratio = memento.getInteger("ratio");
		if (ratio != null)
			sashForm.setWeights(new int[] { ratio.intValue(),
					1000 - ratio.intValue() });
		String scrollLock = memento.getString("scroll");
		if (scrollLock != null) {
			scrollLockAction.setChecked(scrollLock.equals("true"));
			setAutoScroll(!scrollLockAction.isChecked());
		}
		String failuresOnly = memento.getString("failuresOnly");
		boolean showFailuresOnly = false;
		if (failuresOnly != null) {
			showFailuresOnly = Boolean.parseBoolean(failuresOnly);
		}
		setShowFailuresOnly(showFailuresOnly);
		String time = memento.getString("time");
		boolean showTime = true;
		if (time != null) {
			showTime = time.equals("true");
		}
		setShowExecutionTime(showTime);
	}

	@Override
	public void saveState(IMemento memento) {
		if (sashForm == null) {
			if (this.memento != null) {
				memento.putMemento(this.memento);
			}
			return;
		}
		memento.putString("scroll",
				String.valueOf(scrollLockAction.isChecked()));
		int[] weigths = sashForm.getWeights();
		int ratio = weigths[0] * 1000 / (weigths[0] + weigths[1]);
		memento.putInteger("ratio", ratio);
		memento.putString("failuresOnly",
				String.valueOf(failuresOnlyFilterAction.isChecked()));
		memento.putString("time", String.valueOf(showTimeAction.isChecked()));
	}

	@Override
	public void setFocus() {
		if (testNodeViewer != null) {
			testNodeViewer.getControl().setFocus();
		}

	}

	public void init(IFile file) {
		try {
			testExecution = TestExecutionHelper.parse(new InputStreamReader(
					file.getContents(true), file.getCharset()));
		} catch (Exception e) {
			UIUtils.showError(getViewSite().getShell(), "Error",
					"Cannot open \"" + file.getRawLocation().toOSString()
							+ "\"", e);
		}
	}

	public IJavaProject getLaunchedProject() {
		String projectName = testExecution != null ? testExecution.getArgs()
				.get(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME)
				: null;
		if (!Strings.isBlank(projectName)) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			IJavaProject javaProject = JavaCore.create(project);
			if (javaProject != null && javaProject.exists()) {
				return javaProject;
			}
		}
		return null;
	}

	@Override
	public synchronized void dispose() {
		disposed = true;
		disposeImages();
		if (clipboard != null) {
			clipboard.dispose();
		}
		super.dispose();
	}

	private void disposeImages() {
		scriptIcon.dispose();
		scriptPassIcon.dispose();
		scriptErrorIcon.dispose();
		scriptFailureIcon.dispose();
		scriptBlockedIcon.dispose();
		scriptRunningIcon.dispose();
		methodIcon.dispose();
		methodPassIcon.dispose();
		methodErrorIcon.dispose();
		methodFailureIcon.dispose();
		methodBlockedIcon.dispose();
		methodRunningIcon.dispose();
		loopIcon.dispose();
		loopPassIcon.dispose();
		loopErrorIcon.dispose();
		loopFailureIcon.dispose();
		loopBlockedIcon.dispose();
		loopRunningIcon.dispose();
	}

	private boolean isDisposed() {
		return disposed || counterPanel.isDisposed();
	}

	public void handleTestSelected(final TestElement testElement) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (!isDisposed()) {
					failureTrace.showFailure(testElement);
				}
			}
		});
	}

	private void postSyncRunnable(Runnable r) {
		if (!isDisposed()) {
			getViewSite().getShell().getDisplay().syncExec(r);
		}
	}

	public boolean isAutoScroll() {
		return autoScroll;
	}

	public void setAutoScroll(boolean autoScroll) {
		this.autoScroll = autoScroll;
	}
}
