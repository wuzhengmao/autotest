package cn.shaviation.autotest.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class FailureTableDisplay {

	private final Table table;

	private final Image exceptionIcon = UIUtils.getImage("exc_catch.gif");
	private final Image stackIcon = UIUtils.getImage("stkfrm_obj.gif");

	public FailureTableDisplay(Table table) {
		this.table = table;
		table.getParent().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeIcons();
			}
		});
	}

	public void addTraceLine(int lineType, String label) {
		TableItem tableItem = newTableItem();
		switch (lineType) {
		case TextualTrace.LINE_TYPE_EXCEPTION:
			tableItem.setImage(exceptionIcon);
			break;
		case TextualTrace.LINE_TYPE_STACKFRAME:
			tableItem.setImage(stackIcon);
			break;
		}
		tableItem.setText(label);
	}

	public Image getExceptionIcon() {
		return exceptionIcon;
	}

	public Image getStackIcon() {
		return stackIcon;
	}

	public Table getTable() {
		return table;
	}

	private void disposeIcons() {
		if (exceptionIcon != null && !exceptionIcon.isDisposed()) {
			exceptionIcon.dispose();
		}
		if (stackIcon != null && !stackIcon.isDisposed()) {
			stackIcon.dispose();
		}
	}

	TableItem newTableItem() {
		return new TableItem(table, SWT.NONE);
	}
}
