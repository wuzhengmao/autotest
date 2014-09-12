package cn.shaviation.autotest.ui.internal.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
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
import org.eclipse.ui.part.ViewPart;

import cn.shaviation.autotest.ui.internal.util.EmptyLayout;
import cn.shaviation.autotest.ui.internal.util.ImageUtils;
import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class TestExecutionViewPart extends ViewPart {

	private Composite parentComposite;
	private Clipboard clipboard;
	private Composite counterComposite;
	private CounterPanel counterPanel;
	private TestProgressBar progressBar;
	private SashForm sashForm;
	private FailureTrace failureTrace;

	private int currentOrientation;
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
		counterComposite = createProgressCountPanel(parent);
		counterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sashForm = createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
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
		this.fTestViewer = new TestViewer(top, clipboard, this);
		top.setContent(this.fTestViewer.getTestViewerControl());
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

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void init(IFile file) {

	}

	@Override
	public synchronized void dispose() {
		disposeImages();
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
}
