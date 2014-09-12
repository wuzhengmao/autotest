package cn.shaviation.autotest.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class CounterPanel extends Composite {

	private Text numberOfErrors;
	private Text numberOfFailures;
	private Text numberOfBlocked;
	private Text numberOfRuns;
	private int total;

	private final Image errorIcon = UIUtils.getImage("error_ovr.gif");
	private final Image failureIcon = UIUtils.getImage("failed_ovr.gif");
	private final Image blockedIcon = UIUtils.getImage("blocked_ovr.gif");

	public CounterPanel(Composite parent) {
		super(parent, SWT.WRAP);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 12;
		gridLayout.makeColumnsEqualWidth = false;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		numberOfRuns = createLabel("Runs:", null, " 0/0  ");
		numberOfErrors = createLabel("Errors:", errorIcon, " 0 ");
		numberOfFailures = createLabel("Failures:", failureIcon, " 0 ");
		numberOfBlocked = createLabel("Blocked:", blockedIcon, " 0 ");
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeIcons();
			}
		});
	}

	private void disposeIcons() {
		errorIcon.dispose();
		failureIcon.dispose();
		blockedIcon.dispose();
	}

	private Text createLabel(String name, Image image, String init) {
		Label label = new Label(this, SWT.NONE);
		if (image != null) {
			image.setBackground(label.getBackground());
			label.setImage(image);
		}
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		label = new Label(this, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		Text value = new Text(this, SWT.READ_ONLY);
		value.setText(init);
		value.setBackground(getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_BEGINNING));
		return value;
	}

	public void reset() {
		setErrorValue(0);
		setFailureValue(0);
		setBlockedValue(0);
		setRunValue(0);
		total = 0;
	}

	public void setTotal(int value) {
		total = value;
	}

	public int getTotal() {
		return total;
	}

	public void setRunValue(int value) {
		String runString = value + "/" + total;
		numberOfRuns.setText(runString);
		numberOfRuns.redraw();
		redraw();
	}

	public void setErrorValue(int value) {
		numberOfErrors.setText(Integer.toString(value));
		redraw();
	}

	public void setFailureValue(int value) {
		numberOfFailures.setText(Integer.toString(value));
		redraw();
	}

	public void setBlockedValue(int value) {
		numberOfBlocked.setText(Integer.toString(value));
		redraw();
	}
}
