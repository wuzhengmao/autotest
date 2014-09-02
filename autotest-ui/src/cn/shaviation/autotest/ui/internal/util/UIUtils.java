package cn.shaviation.autotest.ui.internal.util;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import cn.shaviation.autotest.core.util.PropertyChangeSupportBean;
import cn.shaviation.autotest.ui.AutoTestUI;
import cn.shaviation.autotest.ui.internal.databinding.Jsr303BeanValidator;

public abstract class UIUtils {

	private static ResourceManager resourceManager;

	public static ImageDescriptor getImageDescriptor(String name) {
		return AutoTestUI.imageDescriptorFromPlugin(AutoTestUI.PLUGIN_ID,
				"icons/" + name);
	}

	public static Image getImage(ImageDescriptor imageDescriptor) {
		return (Image) getResourceManager().get(imageDescriptor);
	}

	public static Image getImage(String name) {
		return (Image) getResourceManager().get(getImageDescriptor(name));
	}

	public static ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(
					JFaceResources.getResources());
		}
		return resourceManager;
	}

	public static void showError(IEditorPart editorPart, String message,
			Throwable t) {
		showError(editorPart.getSite().getShell(), "Error", message, t);
	}

	public static void showError(Shell shell, String title, String message) {
		ErrorDialog.openError(shell, title, message, new Status(Status.ERROR,
				AutoTestUI.PLUGIN_ID, message));
	}

	public static void showError(Shell shell, String title, String message,
			Throwable t) {
		ErrorDialog.openError(shell, title, message, new Status(Status.ERROR,
				AutoTestUI.PLUGIN_ID, t.getMessage(), t));
	}

	public static void setReadonly(Composite composite, boolean readonly) {
		if (composite != null) {
			for (Control control : composite.getChildren()) {
				if ((control instanceof Text)) {
					if ((control.getStyle() & SWT.BORDER) != 0) {
						((Text) control).setEditable(!readonly);
					}
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

	public static TableWrapLayout createFormTableWrapLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 12;
		layout.bottomMargin = 12;
		layout.leftMargin = 6;
		layout.rightMargin = 6;
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = 17;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;
		return layout;
	}

	public static TableWrapLayout createFormPaneTableWrapLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = 17;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;
		return layout;
	}

	public static TableWrapLayout createClearTableWrapLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 2;
		layout.bottomMargin = 2;
		layout.leftMargin = 2;
		layout.rightMargin = 2;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;
		return layout;
	}

	public static TableWrapLayout createSectionClientTableWrapLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 5;
		layout.bottomMargin = 5;
		layout.leftMargin = 2;
		layout.rightMargin = 2;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 5;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;
		return layout;
	}

	public static GridLayout createFormGridLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 12;
		layout.marginBottom = 12;
		layout.marginLeft = 6;
		layout.marginRight = 6;
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = 17;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;
		return layout;
	}

	public static GridLayout createFormPaneGridLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = 17;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;
		return layout;
	}

	public static GridLayout createClearGridLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 2;
		layout.marginBottom = 2;
		layout.marginLeft = 2;
		layout.marginRight = 2;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;
		return layout;
	}

	public static GridLayout createSectionClientGridLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 2;
		layout.marginRight = 2;
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;
		return layout;
	}

	public static GridLayout createButtonsGridLayout() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}

	public static Binding bindText(DataBindingContext dataBindingContext,
			IManagedForm managedForm, Control control, Object bean,
			String propName) {
		IObservableValue observeWidget = control instanceof Text
				|| control instanceof StyledText ? WidgetProperties.text(
				SWT.Modify).observe(control) : WidgetProperties.text().observe(
				control);
		return bind(dataBindingContext, observeWidget, managedForm, control,
				bean, propName, null, null);
	}

	public static Binding bindText(DataBindingContext dataBindingContext,
			IManagedForm managedForm, Control control, Object bean,
			String propName, Converter targetToModelConverter,
			Converter modelToTargetConverter) {
		IObservableValue observeWidget = control instanceof Text
				|| control instanceof StyledText ? WidgetProperties.text(
				SWT.Modify).observe(control) : WidgetProperties.text().observe(
				control);
		return bind(dataBindingContext, observeWidget, managedForm, control,
				bean, propName, targetToModelConverter, modelToTargetConverter);
	}

	public static Binding bindSelection(DataBindingContext dataBindingContext,
			IManagedForm managedForm, Viewer viewer, Object bean,
			String propName) {
		IObservableValue observeWidget = ViewerProperties.singleSelection()
				.observe(viewer);
		return bind(dataBindingContext, observeWidget, managedForm,
				viewer.getControl(), bean, propName, null, null);
	}

	public static Binding bindSelection(DataBindingContext dataBindingContext,
			IManagedForm managedForm, Viewer viewer, Object bean,
			String propName, Converter targetToModelConverter,
			Converter modelToTargetConverter) {
		IObservableValue observeWidget = ViewerProperties.singleSelection()
				.observe(viewer);
		return bind(dataBindingContext, observeWidget, managedForm,
				viewer.getControl(), bean, propName, targetToModelConverter,
				modelToTargetConverter);
	}

	private static Binding bind(DataBindingContext dataBindingContext,
			IObservableValue observeWidget, IManagedForm managedForm,
			Control control, Object bean, String propName,
			Converter targetToModelConverter, Converter modelToTargetConverter) {
		IObservableValue observeValue = bean instanceof PropertyChangeSupportBean ? BeanProperties
				.value(propName).observe(bean) : PojoProperties.value(propName)
				.observe(bean);
		UpdateValueStrategy targetToModel = new UpdateValueStrategy();
		targetToModel.setConverter(targetToModelConverter);
		targetToModel.setAfterConvertValidator(new Jsr303BeanValidator(bean
				.getClass(), propName, managedForm, control));
		UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
		modelToTarget.setConverter(modelToTargetConverter);
		Binding binding = dataBindingContext.bindValue(observeWidget,
				observeValue, targetToModel, modelToTarget);
		return binding;
	}
}
