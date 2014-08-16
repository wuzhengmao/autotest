package cn.shaviation.autotest.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.IMessagePrefixProvider;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.util.Converters;
import cn.shaviation.autotest.util.DocumentListenerAdapter;
import cn.shaviation.autotest.util.UIUtils;

public class TestDataFormPage extends FormPage {

	private Text nameText;
	private Text descText;
	private Text authorText;
	private Label modifyTime;
	private DataBindingContext dataBindingContext;

	private DefaultModifyListener defaultModifyListener = new DefaultModifyListener();
	private boolean ignoreChange = false;
	private boolean ignoreReload = false;
	private boolean needReload = true;
	private boolean documentError = false;
	private long lastModifyTime;

	private IDocumentListener documentListener = new DocumentListenerAdapter() {
		@Override
		public void documentChanged(DocumentEvent event) {
			if (!ignoreReload) {
				needReload = true;
				if (isActive() && getEditor().isActive()) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							loadTestData();
							needReload = false;
						}
					});
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
		body.setLayout(UIUtils.createFormTableWrapLayout(true, 2));
		Composite leftComposite = toolkit.createComposite(body, SWT.NONE);
		leftComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		leftComposite
				.setLayout(UIUtils.createFormPaneTableWrapLayout(false, 1));
		createGeneralSection(toolkit, leftComposite);
		createGroupSection(toolkit, leftComposite);
		Composite rightComposite = toolkit.createComposite(body, SWT.NONE);
		rightComposite
				.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		rightComposite.setLayout(UIUtils
				.createFormPaneTableWrapLayout(false, 1));
		createDataSection(toolkit, rightComposite);
		IToolBarManager toolBarManager = form.getToolBarManager();
		toolBarManager.add(new Action("Refresh", UIUtils
				.getImage("refresh.gif")) {
			public void run() {
				getEditor().getSourcePage()
						.getAction(ITextEditorActionConstants.REFRESH).run();
			}
		});
		form.updateToolBar();
		toolkit.decorateFormHeading(form.getForm());
		form.getMessageManager().setMessagePrefixProvider(
				new IMessagePrefixProvider() {
					@Override
					public String getPrefix(Control control) {
						return null;
					}
				});
	}

	private void createGeneralSection(FormToolkit toolkit, Composite container) {
		Section section = toolkit.createSection(container, SWT.HORIZONTAL);
		section.setText("General Information");
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setLayout(UIUtils.createClearTableWrapLayout(false, 1));
		Composite client = toolkit.createComposite(section);
		client.setLayout(UIUtils.createSectionClientTableWrapLayout(false, 2));
		section.setClient(client);
		toolkit.createLabel(client, "Name:").setLayoutData(
				new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));
		nameText = toolkit.createText(client, null, SWT.NONE);
		nameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB,
				TableWrapData.MIDDLE));
		nameText.addModifyListener(defaultModifyListener);
		toolkit.createLabel(client, "Description:").setLayoutData(
				new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));
		descText = toolkit.createText(client, null, SWT.MULTI);
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB,
				TableWrapData.MIDDLE);
		td.heightHint = 72;
		descText.setLayoutData(td);
		descText.addModifyListener(defaultModifyListener);
		toolkit.createLabel(client, "Author:").setLayoutData(
				new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));
		authorText = toolkit.createText(client, null, SWT.NONE);
		authorText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB,
				TableWrapData.MIDDLE));
		authorText.addModifyListener(defaultModifyListener);
		toolkit.createLabel(client, "Last modified:").setLayoutData(
				new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));
		modifyTime = toolkit.createLabel(client, null);
		modifyTime.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB,
				TableWrapData.MIDDLE));
		toolkit.paintBordersFor(client);
	}

	private void createGroupSection(FormToolkit toolkit, Composite container) {
		Section section = toolkit.createSection(container, SWT.HORIZONTAL);
		section.setText("Test Data Groups");
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setLayout(UIUtils.createClearTableWrapLayout(false, 1));
		Composite client = toolkit.createComposite(section);
		client.setLayout(UIUtils.createSectionClientTableWrapLayout(false, 2));
		section.setClient(client);
		toolkit.paintBordersFor(client);
	}

	private void createDataSection(FormToolkit toolkit, Composite container) {
		Section section = toolkit.createSection(container, SWT.HORIZONTAL);
		section.setText("Test Data Details");
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setLayout(UIUtils.createClearTableWrapLayout(false, 1));
		Composite client = toolkit.createComposite(section);
		client.setLayout(UIUtils.createSectionClientTableWrapLayout(false, 2));
		section.setClient(client);
		toolkit.paintBordersFor(client);
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
		unbindControls();
		boolean dirty = getEditor().isDirty();
		TestDataDef testDataDef = new TestDataDef();
		IStatus status = getEditor().checkDocumentStatus();
		if (status != null) {
			setErrorMessage(status.getMessage(), IMessageProvider.WARNING);
			documentError = true;
		} else {
			try {
				String json = getEditorInput().getDocument().get();
				if (json != null && !json.isEmpty()) {
					testDataDef = getEditorInput().getObjectMapper().readValue(
							json, TestDataDef.class);
				} else {
					testDataDef.setAuthor(System.getProperty("user.name"));
					setErrorMessage("No content", IMessageProvider.WARNING);
				}
				clearErrorMessage();
				documentError = false;
			} catch (Exception e) {
				setErrorMessage(e.getMessage(), IMessageProvider.ERROR);
				documentError = true;
			}
		}
		getEditorInput().setTestDataDef(testDataDef);
		boolean readonly = documentError;
		if (!readonly) {
			try {
				readonly = getEditorInput().getStorage().isReadOnly();
			} catch (CoreException e) {
			}
		}
		UIUtils.setReadonly((Composite) getPartControl(), readonly);
		if (!documentError) {
			ignoreChange = true;
			bindControls();
			ignoreChange = false;
		}
		lastModifyTime = 0;
		if (!dirty) {
			createProblems();
		}
	}

	public void createProblems() {
		UIUtils.deleteProblems(getEditorInput().getFile());
		int severity = toProblemSeverity(getManagedForm().getForm().getForm()
				.getMessageType());
		if (severity > 0) {
			IMessage[] messages = getManagedForm().getForm().getForm()
					.getChildrenMessages();
			if (messages != null && messages.length > 0) {
				for (IMessage message : messages) {
					int sev = toProblemSeverity(message.getMessageType());
					if (sev > 0) {
						UIUtils.addProblem(
								getEditorInput().getFile(),
								getManagedForm().getMessageManager()
										.createSummary(
												new IMessage[] { message }),
								sev);
					}
				}
			} else {
				UIUtils.addProblem(getEditorInput().getFile(), getManagedForm()
						.getForm().getForm().getMessage(), severity);
			}
		}
	}

	private int toProblemSeverity(int messageType) {
		switch (messageType) {
		case IMessageProvider.ERROR:
			return IMarker.SEVERITY_ERROR;
		case IMessageProvider.WARNING:
			return IMarker.SEVERITY_WARNING;
		case IMessageProvider.INFORMATION:
			return IMarker.SEVERITY_INFO;
		default:
			return -1;
		}
	}

	public String getErrorMessage() {
		if (getManagedForm().getForm().getMessageType() == IMessageProvider.ERROR
				|| getManagedForm().getForm().getMessageType() == IMessageProvider.WARNING) {
			String error = getManagedForm().getMessageManager().createSummary(
					getManagedForm().getForm().getForm().getChildrenMessages());
			if (error == null || error.isEmpty()) {
				error = getManagedForm().getForm().getMessage();
			}
			return error;
		}
		return null;
	}

	private void clearErrorMessage() {
		UIUtils.setMessage(getManagedForm().getForm(), null,
				IMessageProvider.NONE);
	}

	private void setErrorMessage(final String msg, int severity) {
		if ((getPartControl() != null) && (!getPartControl().isDisposed())) {
			if (!getManagedForm().getForm().isDisposed()) {
				UIUtils.setMessage(getManagedForm().getForm(), msg, severity);
			}
		}
	}

	private void bindControls() {
		IManagedForm managedForm = getManagedForm();
		TestDataDef testDataDef = getEditorInput().getTestDataDef();
		dataBindingContext = new DataBindingContext();
		UIUtils.bindText(dataBindingContext, managedForm, nameText,
				testDataDef, "name", Converters.TRIM, Converters.TRIM);
		UIUtils.bindText(dataBindingContext, managedForm, descText,
				testDataDef, "description", Converters.TRIM, Converters.TRIM);
		UIUtils.bindText(dataBindingContext, managedForm, authorText,
				testDataDef, "author", Converters.TRIM, Converters.TRIM);
		UIUtils.bindText(dataBindingContext, managedForm, modifyTime,
				testDataDef, "lastUpdateTime", null, Converters.DATESTAMP);
	}

	private void unbindControls() {
		if (dataBindingContext != null) {
			dataBindingContext.dispose();
			dataBindingContext = null;
		}
		getManagedForm().getMessageManager().removeAllMessages();
	}

	private void onFormChange() {
		if (!ignoreChange) {
			lastModifyTime = System.nanoTime();
			if (!getEditor().isDirty()) {
				ignoreReload = true;
				try {
					((IDocumentExtension4) getEditorInput().getDocument())
							.replace(0, 0, "", System.currentTimeMillis());
				} catch (Exception e) {
					getEditorInput().getDocument().set(
							getEditorInput().getDocument().get());
				}
				ignoreReload = false;
				getEditor().editorDirtyStateChanged();
			}
		}
	}

	public boolean isDocumentError() {
		return documentError;
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
