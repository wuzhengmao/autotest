package cn.shaviation.autotest.util;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import cn.shaviation.autotest.AutoTestPlugin;

public abstract class Utils {

	public static ImageDescriptor getImage(String name) {
		return AutoTestPlugin.imageDescriptorFromPlugin(
				AutoTestPlugin.PLUGIN_ID, "icons/" + name);
	}

	public static void showError(IEditorPart editorPart, String message,
			Throwable t) {
		ErrorDialog.openError(
				editorPart.getSite().getShell(),
				"Error",
				message,
				new Status(Status.ERROR, AutoTestPlugin.PLUGIN_ID, t
						.getMessage(), t));
	}

	public static void setReadonly(Composite composite, boolean readonly) {
		if (composite != null) {
			for (Control control : composite.getChildren()) {
				if ((control instanceof Text)) {
					((Text) control).setEditable(!readonly);
				} else if ((control instanceof Combo)) {
					((Combo) control).setEnabled(!readonly);
				} else if ((control instanceof CCombo)) {
					((CCombo) control).setEnabled(!readonly);
				} else if ((control instanceof Button)) {
					((Button) control).setEnabled(!readonly);
				} else if ((control instanceof Composite)) {
					setReadonly((Composite) control, readonly);
				}
			}
		}
	}

	public static void setMessage(ScrolledForm form, String message,
			int severity) {
		if ((message != null)
				&& ((message.length() > 80) || (message.contains("\n")))) {
			String truncMsg = message;
			String[] lines = message.split("\n");
			if (lines.length > 0)
				truncMsg = lines[0];
			else {
				truncMsg = message.substring(0, 80);
			}
			form.getForm().setMessage(truncMsg, severity);
		} else {
			form.getForm().setMessage(message, severity);
		}
	}
}
