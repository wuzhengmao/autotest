package cn.shaviation.autotest.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.util.Utils;

public class TestDataEditorPage extends TestDataFormPage {

	private Text idText;

	private DefaultModifyListener defaultModifyListener = new DefaultModifyListener();
	private boolean ignoreChange = false;

	public TestDataEditorPage(TestDataEditor editor) {
		super(editor, "cn.shaviation.autotest.editors.TestDataEditorPage",
				"Visual Editor");
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.setText("Test Data Editor");
		Composite body = form.getBody();
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.horizontalSpacing = 7;
		body.setLayout(gridLayout);
		toolkit.paintBordersFor(body);
		Composite leftComposite = toolkit.createComposite(body, 0);
		leftComposite
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout leftCompositeLayout = new GridLayout();
		leftCompositeLayout.marginWidth = 0;
		leftCompositeLayout.marginHeight = 0;
		leftComposite.setLayout(leftCompositeLayout);
		createGeneralSection(toolkit, leftComposite);
		toolkit.paintBordersFor(leftComposite);
		super.createFormContent(managedForm);
	}

	private void createGeneralSection(FormToolkit toolkit, Composite container) {
		Section section = toolkit.createSection(container, SWT.HORIZONTAL);
		section.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		section.setText("General Information");
		Composite composite = toolkit.createComposite(section, SWT.NONE);
		toolkit.adapt(composite);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginBottom = 5;
		gridLayout.marginHeight = 2;
		gridLayout.marginWidth = 1;
		composite.setLayout(gridLayout);
		section.setClient(composite);
		Label idLabel = toolkit.createLabel(composite, "ID:", SWT.NONE);
		idLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		idText = toolkit.createText(composite, null, SWT.NONE);
		idText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		idText.addModifyListener(defaultModifyListener);
		toolkit.paintBordersFor(composite);
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		boolean readonly = true;
		try {
			readonly = getEditorInput().getStorage().isReadOnly();
		} catch (CoreException e) {
		}
		Utils.setReadonly((Composite) getPartControl(), readonly);
	}

	@Override
	protected void fillForm(TestDataDef testDataDef) {
		ignoreChange = true;
		DataBindingContext dataBindingContext = new DataBindingContext();
		IObservableValue observeWidget = WidgetProperties.text(SWT.Modify)
				.observe(idText);
		IObservableValue observeValue = PojoProperties.value("id").observe(
				testDataDef);
		dataBindingContext.bindValue(observeWidget, observeValue);
		ignoreChange = false;
	}

	private class DefaultModifyListener implements ModifyListener,
			ISelectionChangedListener, SelectionListener,
			PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (!ignoreChange) {
				getEditor().setDirty(true);
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			if (!ignoreChange) {
				getEditor().setDirty(true);
			}
		}

		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!ignoreChange) {
				getEditor().setDirty(true);
			}
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (!ignoreChange) {
				getEditor().setDirty(true);
			}
		}

		@Override
		public void modifyText(ModifyEvent event) {
			if (!ignoreChange) {
				getEditor().setDirty(true);
			}
		}
	}
}
