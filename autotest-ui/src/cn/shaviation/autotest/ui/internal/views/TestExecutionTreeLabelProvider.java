package cn.shaviation.autotest.ui.internal.views;

import java.text.NumberFormat;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import cn.shavation.autotest.runner.TestElement;
import cn.shavation.autotest.runner.TestElement.Type;
import cn.shaviation.autotest.ui.internal.views.TestExecutionTreeContentProvider.TreeNode;
import cn.shaviation.autotest.util.Strings;

public class TestExecutionTreeLabelProvider extends LabelProvider implements
		IStyledLabelProvider {

	private static final NumberFormat TIME_FORMAT;

	private final TestExecutionViewPart testExecutionView;
	private boolean showTime;

	static {
		TIME_FORMAT = NumberFormat.getNumberInstance();
		TIME_FORMAT.setGroupingUsed(true);
		TIME_FORMAT.setMinimumFractionDigits(3);
		TIME_FORMAT.setMaximumFractionDigits(3);
		TIME_FORMAT.setMinimumIntegerDigits(1);
	}

	public TestExecutionTreeLabelProvider(
			TestExecutionViewPart testExecutionView) {
		this.testExecutionView = testExecutionView;
		this.showTime = true;
	}

	@Override
	public StyledString getStyledText(Object element) {
		TestElement testElement = ((TreeNode) element).getElement();
		String label = testElement.getName();
		if (label == null) {
			return new StyledString(element.toString());
		}
		String detail = null;
		if ((testElement.getType() == Type.METHOD || testElement.getType() == Type.SCRIPT)
				&& label.endsWith(")")) {
			int i = label.lastIndexOf('(');
			if (i >= 0) {
				detail = label.substring(i + 1, label.length() - 1).trim();
				label = label.substring(0, i).trim();
			}
		}
		StyledString text = new StyledString(label);
		if (!Strings.isEmpty(detail)) {
			String decorated = label + " [" + detail + "]";
			text = StyledCellLabelProvider.styleDecoratedString(decorated,
					StyledString.QUALIFIER_STYLER, text);
		}
		return addElapsedTime(text, testElement.getRunTime() / 1000d);
	}

	private StyledString addElapsedTime(StyledString styledString, double time) {
		String string = styledString.getString();
		String decorated = addElapsedTime(string, time);
		return StyledCellLabelProvider.styleDecoratedString(decorated,
				StyledString.COUNTER_STYLER, styledString);
	}

	private String addElapsedTime(String string, double time) {
		if ((!this.showTime) || (Double.isNaN(time))) {
			return string;
		}
		String formattedTime = TIME_FORMAT.format(time);
		return string + " (" + formattedTime + " s)";
	}

	@Override
	public String getText(Object element) {
		TestElement testElement = ((TreeNode) element).getElement();
		String label = testElement.getName();
		if (label == null) {
			return element.toString();
		}
		String detail = null;
		if ((testElement.getType() == Type.METHOD || testElement.getType() == Type.SCRIPT)
				&& label.endsWith(")")) {
			int i = label.lastIndexOf('(');
			if (i >= 0) {
				detail = label.substring(i + 1, label.length() - 1).trim();
				label = label.substring(0, i).trim();
			}
		}
		if (!Strings.isEmpty(detail)) {
			label = label + " [" + detail + "]";
		}
		if (testElement.getRunTime() != null) {
			return addElapsedTime(label, testElement.getRunTime() / 1000d);
		} else {
			return label;
		}
	}

	@Override
	public Image getImage(Object element) {
		TestElement testElement = ((TreeNode) element).getElement();
		switch (testElement.getType()) {
		case LOOP:
			switch (testElement.getStatus()) {
			case PASS:
				return testExecutionView.loopPassIcon;
			case ERROR:
				return testExecutionView.loopErrorIcon;
			case FAILURE:
				return testExecutionView.loopFailureIcon;
			case BLOCKED:
				return testExecutionView.loopBlockedIcon;
			case RUNNING:
				return testExecutionView.loopRunningIcon;
			default:
				return testExecutionView.loopIcon;
			}
		case METHOD:
			switch (testElement.getStatus()) {
			case PASS:
				return testExecutionView.methodPassIcon;
			case ERROR:
				return testExecutionView.methodErrorIcon;
			case FAILURE:
				return testExecutionView.methodFailureIcon;
			case BLOCKED:
				return testExecutionView.methodBlockedIcon;
			case RUNNING:
				return testExecutionView.methodRunningIcon;
			default:
				return testExecutionView.methodIcon;
			}
		default:
			switch (testElement.getStatus()) {
			case PASS:
				return testExecutionView.scriptPassIcon;
			case ERROR:
				return testExecutionView.scriptErrorIcon;
			case FAILURE:
				return testExecutionView.scriptFailureIcon;
			case BLOCKED:
				return testExecutionView.scriptBlockedIcon;
			case RUNNING:
				return testExecutionView.scriptRunningIcon;
			default:
				return testExecutionView.scriptIcon;
			}
		}
	}

	public void setShowTime(boolean showTime) {
		this.showTime = showTime;
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}
}
