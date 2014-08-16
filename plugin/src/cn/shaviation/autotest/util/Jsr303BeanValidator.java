package cn.shaviation.autotest.util;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;

public class Jsr303BeanValidator implements IValidator {

	@SuppressWarnings("rawtypes")
	private Class clazz;
	private String propName;
	private IManagedForm managedForm;
	private Control control;

	public Jsr303BeanValidator(Class<?> clazz, String propName,
			IManagedForm managedForm) {
		this.clazz = clazz;
		this.propName = propName;
		this.managedForm = managedForm;
	}

	public Jsr303BeanValidator(Class<?> clazz, String propName,
			IManagedForm managedForm, Control control) {
		this(clazz, propName, managedForm);
		this.control = control;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IStatus validate(Object value) {
		String errorText = UIUtils.getErrorMessage(Validators.validateValue(
				clazz, propName, value));
		String key = clazz.getName() + "#" + propName;
		IMessageManager messageManager = managedForm.getMessageManager();
		if (errorText != null) {
			if (control != null) {
				messageManager.addMessage(key, errorText, null,
						IMessageProvider.ERROR, control);
			} else {
				messageManager.addMessage(key, errorText, null,
						IMessageProvider.ERROR);
			}
		} else {
			if (control != null) {
				messageManager.removeMessage(key, control);
			} else {
				messageManager.removeMessage(key);
			}
		}
		return ValidationStatus.ok();
	}
}
