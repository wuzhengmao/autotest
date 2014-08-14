package cn.shaviation.autotest.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
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
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.util.DocumentListenerAdapter;
import cn.shaviation.autotest.util.Utils;

public class TestDataFormPage extends FormPage {

	private Text idText;
	private DataBindingContext dataBindingContext;

	private DefaultModifyListener defaultModifyListener = new DefaultModifyListener();
	private boolean ignoreChange = false;
	private boolean ignoreReload = false;
	private boolean needReload = true;
	private long lastModifyTime;

	private IDocumentListener documentListener = new DocumentListenerAdapter() {
		@Override
		public void documentChanged(DocumentEvent event) {
			if (!ignoreReload) {
				needReload = true;
				if (isActive() && getEditor().isActive()) {
					loadTestData();
					needReload = false;
				}
			}
		}
	};

	public TestDataFormPage(TestDataEditor editor) {
		super(editor, "cn.shaviation.autotest.editors.TestDataFormPage",
				"Visual Editor");
	}

	@Override
	public TestDataEditor getEditor() {
		return (TestDataEditor) super.getEditor();
	}

	@Override
	public TestDataEditorInput getEditorInput() {
		return (TestDataEditorInput) super.getEditorInput();
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
		IToolBarManager toolBarManager = form.getToolBarManager();
		toolBarManager
				.add(new Action("Refresh", Utils.getImage("refresh.gif")) {
					public void run() {
						getEditor().getSourcePage()
								.getAction(ITextEditorActionConstants.REFRESH)
								.run();
					}
				});
		form.updateToolBar();
		toolkit.decorateFormHeading(form.getForm());
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

	public void onDocumentProviderChange(IDocumentProvider documentProvider) {
		if (documentProvider != null
				&& documentProvider.getDocument(getEditorInput()) != null) {
			documentProvider.getDocument(getEditorInput())
					.removeDocumentListener(documentListener);
		}
		if (getEditorInput().getDocumentProvider() != null) {
			getEditorInput().getDocumentProvider()
					.getDocument(getEditorInput())
					.addDocumentListener(documentListener);
		}
	}

	public void onActive() {
		if (needReload) {
			loadTestData();
			needReload = false;
		}
	}

	private void loadTestData() {
		TestDataDef testDataDef = new TestDataDef();
		IStatus status = getEditor().checkDocumentStatus();
		if (status != null) {
			setErrorMessage(status.getMessage(), IMessageProvider.WARNING);
		} else {
			try {
				String json = getEditorInput().getDocument().get();
				if (json != null && !json.isEmpty()) {
					testDataDef = getEditorInput().getObjectMapper().readValue(
							json, TestDataDef.class);
				}
				clearErrorMessage();
			} catch (Exception e) {
				setErrorMessage(e.getMessage(), IMessageProvider.ERROR);
				Utils.setReadonly((Composite) getPartControl(), true);
			}
		}
		getEditorInput().setTestDataDef(testDataDef);
		unbindControls();
		ignoreChange = true;
		bindControls();
		ignoreChange = false;
		boolean readonly = isError();
		if (!readonly) {
			try {
				readonly = getEditorInput().getStorage().isReadOnly();
			} catch (CoreException e) {
			}
		}
		Utils.setReadonly((Composite) getPartControl(), readonly);
		lastModifyTime = 0;
	}

	private boolean isError() {
		return getManagedForm().getForm().getMessageType() == IMessageProvider.ERROR
				|| getManagedForm().getForm().getMessageType() == IMessageProvider.WARNING;
	}

	private void clearErrorMessage() {
		Utils.setMessage(getManagedForm().getForm(), null,
				IMessageProvider.NONE);
	}

	private void setErrorMessage(final String msg, int severity) {
		if ((getPartControl() != null) && (!getPartControl().isDisposed())) {
			if (!getManagedForm().getForm().isDisposed()) {
				Utils.setMessage(getManagedForm().getForm(), msg, severity);
			}
		}
	}

	private void bindControls() {
		dataBindingContext = new DataBindingContext();
		IObservableValue observeWidget = WidgetProperties.text(SWT.Modify)
				.observe(idText);
		IObservableValue observeValue = PojoProperties.value("id").observe(
				getEditorInput().getTestDataDef());
		dataBindingContext.bindValue(observeWidget, observeValue);
	}

	private void unbindControls() {
		if (dataBindingContext != null) {
			dataBindingContext.dispose();
			dataBindingContext = null;
		}
	}

	private void onFormChange() {
		if (!ignoreChange) {
			lastModifyTime = System.nanoTime();
			if (!getEditor().isDirty()) {
				ignoreReload = true;
				getEditorInput().getDocument().set(
						getEditorInput().getDocument().get());
				ignoreReload = false;
				getEditor().fireDirty();
			}
		}
	}

	public long getLastModifyTime() {
		return lastModifyTime;
	}

	private class DefaultModifyListener implements ModifyListener,
			ISelectionChangedListener, SelectionListener,
			PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			onFormChange();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			onFormChange();
		}

		@Override
		public void widgetSelected(SelectionEvent event) {
			onFormChange();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			onFormChange();
		}

		@Override
		public void modifyText(ModifyEvent event) {
			onFormChange();
		}
	}
}
