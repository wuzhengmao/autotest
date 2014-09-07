package cn.shaviation.autotest.ui.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import cn.shaviation.autotest.ui.internal.util.ControlAccessibleListener;
import cn.shaviation.autotest.util.Logs;

public class VMArgumentsBlock extends JavaLaunchTab {

	protected Text fVMArgumentsText;
	private Button fPgrmArgVariableButton;

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		setControl(group);
		GridLayout topLayout = new GridLayout();
		group.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd);
		group.setFont(font);
		group.setText("VM ar&guments:");
		fVMArgumentsText = new Text(group, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL | SWT.WRAP);
		fVMArgumentsText.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
				case SWT.TRAVERSE_ESCAPE:
				case SWT.TRAVERSE_PAGE_PREVIOUS:
				case SWT.TRAVERSE_PAGE_NEXT:
					e.doit = true;
					break;
				case SWT.TRAVERSE_RETURN:
				case SWT.TRAVERSE_TAB_PREVIOUS:
				case SWT.TRAVERSE_TAB_NEXT:
					if ((fVMArgumentsText.getStyle() & SWT.SINGLE) != 0) {
						e.doit = true;
					} else if (fVMArgumentsText.isEnabled()
							|| (e.stateMask & SWT.MODIFIER_MASK) != 0) {
						e.doit = true;
					}
					break;
				}
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 40;
		gd.widthHint = 100;
		fVMArgumentsText.setLayoutData(gd);
		fVMArgumentsText.setFont(font);
		fVMArgumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				scheduleUpdateJob();
			}
		});
		ControlAccessibleListener
				.addListener(fVMArgumentsText, group.getText());
		fPgrmArgVariableButton = createPushButton(group, "Variable&s...", null);
		fPgrmArgVariableButton.setFont(font);
		fPgrmArgVariableButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_END));
		fPgrmArgVariableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
						getShell());
				dialog.open();
				String variable = dialog.getVariableExpression();
				if (variable != null) {
					fVMArgumentsText.insert(variable);
				}
			}
		});
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration
				.removeAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fVMArgumentsText.setText(configuration.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""));
		} catch (CoreException e) {
			setErrorMessage("Exception occurred reading configuration:"
					+ e.getStatus().getMessage());
			Logs.e(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				getAttributeValueFrom(fVMArgumentsText));
	}

	@Override
	public String getName() {
		return "VM Arguments";
	}

	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}

	public void setEnabled(boolean enabled) {
		fVMArgumentsText.setEnabled(enabled);
		fPgrmArgVariableButton.setEnabled(enabled);
	}
}
