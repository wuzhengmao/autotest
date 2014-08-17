package cn.shaviation.autotest.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
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
import cn.shaviation.autotest.model.TestDataEntry;
import cn.shaviation.autotest.model.TestDataGroup;
import cn.shaviation.autotest.util.Converters;
import cn.shaviation.autotest.util.DocumentListenerAdapter;
import cn.shaviation.autotest.util.Jsr303BeanValidator;
import cn.shaviation.autotest.util.Strings;
import cn.shaviation.autotest.util.UIUtils;

public class TestDataFormPage extends FormPage {

	private Text nameText;
	private Text descText;
	private Text authorText;
	private Label modifyTime;
	private TableViewer groupTable;
	private Button newGroupButton;
	private Button removeGroupButton;
	private Button moveGroupUpButton;
	private Button moveGroupDownButton;
	private Button cloneGroupButton;
	private TableViewer entryTable;
	private Button newEntryButton;
	private Button removeEntryButton;
	private Button moveEntryUpButton;
	private Button moveEntryDownButton;
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

	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		if (getEditorInput().getTestDataDef() == null) {
			TestDataDef testDataDef = new TestDataDef();
			testDataDef.setDataList(WritableList
					.withElementType(TestDataGroup.class));
			getEditorInput().setTestDataDef(testDataDef);
		}
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.setText("Test Data Editor");
		Composite body = form.getBody();
		body.setLayout(UIUtils.createFormGridLayout(true, 2));
		Composite leftComposite = toolkit.createComposite(body, SWT.NONE);
		leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		leftComposite.setLayout(UIUtils.createFormPaneGridLayout(false, 1));
		createGeneralSection(toolkit, leftComposite);
		createGroupSection(toolkit, leftComposite);
		Composite rightComposite = toolkit.createComposite(body, SWT.NONE);
		rightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		rightComposite.setLayout(UIUtils.createFormPaneGridLayout(false, 1));
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
		Section section = toolkit.createSection(container, SWT.HORIZONTAL
				| Section.DESCRIPTION);
		section.setText("General Information");
		section.setDescription("This section describes general information about this definition file.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.setLayout(UIUtils.createClearGridLayout(false, 1));
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
		Section section = toolkit.createSection(container, SWT.HORIZONTAL
				| Section.DESCRIPTION);
		section.setText("Test Data Groups");
		section.setDescription("Specify multiple groups of test data with similar specification.");
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		section.setLayout(UIUtils.createClearGridLayout(false, 1));
		Composite client = toolkit.createComposite(section);
		client.setLayout(UIUtils.createSectionClientGridLayout(false, 2));
		section.setClient(client);
		groupTable = new TableViewer(client, SWT.FULL_SELECTION | SWT.V_SCROLL
				| toolkit.getBorderStyle());
		groupTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.adapt(groupTable.getTable(), false, false);
		groupTable.setCellEditors(new CellEditor[] { new TextCellEditor(
				groupTable.getTable(), SWT.LEFT) });
		groupTable.setColumnProperties(new String[] { "name" });
		groupTable.setCellModifier(new ICellModifier() {

			@Override
			public void modify(Object element, String property, Object value) {
				TestDataGroup group = (TestDataGroup) ((Item) element)
						.getData();
				if (!Strings.equals(group.getName(), value)) {
					group.setName((String) value);
					groupTable.update(group, new String[] { "name" });
					onFormChange();
				}
			}

			@Override
			public Object getValue(Object element, String property) {
				return Strings.objToString(((TestDataGroup) element).getName());
			}

			@Override
			public boolean canModify(Object element, String property) {
				return true;
			}
		});
		final Jsr303BeanValidator validator = new Jsr303BeanValidator(
				TestDataGroup.class, "name", getManagedForm(),
				groupTable.getTable());
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		groupTable.setContentProvider(contentProvider);
		groupTable.setLabelProvider(new ObservableMapCellLabelProvider(
				PojoObservables.observeMap(contentProvider.getKnownElements(),
						TestDataGroup.class, "name")) {
			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				validator.validate(
						cell.getText(),
						String.valueOf(groupTable.getTable().indexOf(
								(TableItem) cell.getItem())));
			}
		});
		final WritableList groupTableInput = (WritableList) getEditorInput()
				.getTestDataDef().getDataList();
		groupTable.setInput(groupTableInput);
		groupTableInput.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				onFormChange();
			}
		});
		Composite buttons = toolkit.createComposite(client);
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttons.setLayout(UIUtils.createButtonsGridLayout());
		newGroupButton = toolkit.createButton(buttons, "New", SWT.PUSH);
		newGroupButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		newGroupButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				TestDataGroup group = new TestDataGroup();
				group.setName("New Group");
				group.setEntries(WritableList
						.withElementType(TestDataEntry.class));
				groupTableInput.add(group);
				groupTable.setSelection(new StructuredSelection(group));
				groupTable.editElement(group, 0);
			}
		});
		removeGroupButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		removeGroupButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		removeGroupButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				groupTableInput.remove(((IStructuredSelection) groupTable
						.getSelection()).getFirstElement());
			}
		});
		moveGroupUpButton = toolkit.createButton(buttons, "Up", SWT.PUSH);
		moveGroupUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		moveGroupUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = groupTable.getSelection();
				int sel = groupTable.getTable().getSelectionIndex();
				TestDataGroup group = (TestDataGroup) groupTable
						.getElementAt(sel);
				groupTableInput.remove(group);
				groupTableInput.add(sel - 1, group);
				groupTable.setSelection(selection, true);
			}
		});
		moveGroupDownButton = toolkit.createButton(buttons, "Down", SWT.PUSH);
		moveGroupDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		moveGroupDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = groupTable.getSelection();
				int sel = groupTable.getTable().getSelectionIndex();
				TestDataGroup group = (TestDataGroup) groupTable
						.getElementAt(sel + 1);
				groupTableInput.remove(group);
				groupTableInput.add(sel, group);
				groupTable.setSelection(selection, true);
			}
		});
		cloneGroupButton = toolkit.createButton(buttons, "Clone", SWT.PUSH);
		cloneGroupButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		cloneGroupButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				TestDataGroup group = new TestDataGroup();
				group.setEntries(WritableList
						.withElementType(TestDataEntry.class));
				merge((TestDataGroup) groupTable.getElementAt(groupTable
						.getTable().getSelectionIndex()), group);
				group.setName("Copy of " + group.getName());
				groupTableInput.add(group);
				groupTable.setSelection(new StructuredSelection(group));
				groupTable.editElement(group, 0);
			}
		});
		toolkit.paintBordersFor(client);
		groupTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setGroupTableButtonStates();
			}
		});
	}

	private void setGroupTableButtonStates() {
		int sel = groupTable.getTable().getSelectionIndex();
		removeGroupButton.setEnabled(sel >= 0);
		moveGroupUpButton.setEnabled(sel > 0);
		moveGroupDownButton.setEnabled(sel >= 0
				&& sel < groupTable.getTable().getItemCount() - 1);
		cloneGroupButton.setEnabled(sel >= 0);
	}

	private void createDataSection(FormToolkit toolkit, Composite container) {
		Section section = toolkit.createSection(container, SWT.HORIZONTAL
				| Section.DESCRIPTION);
		section.setText("Test Data Specification");
		section.setDescription("Specify the specification of current selected test data group.");
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		section.setLayout(UIUtils.createClearTableWrapLayout(false, 1));
		Composite client = toolkit.createComposite(section);
		client.setLayout(UIUtils.createSectionClientGridLayout(false, 2));
		section.setClient(client);
		entryTable = new TableViewer(client, SWT.FULL_SELECTION | SWT.V_SCROLL
				| toolkit.getBorderStyle());
		entryTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		entryTable.getTable().setHeaderVisible(true);
		entryTable.getTable().setLinesVisible(true);

		toolkit.adapt(entryTable.getTable(), false, false);
		Composite buttons = toolkit.createComposite(client);
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttons.setLayout(UIUtils.createButtonsGridLayout());
		newEntryButton = toolkit.createButton(buttons, "New", SWT.PUSH);
		newEntryButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		newEntryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TestDataEntry entry = new TestDataEntry();
				((WritableList) entryTable.getInput()).add(entry);
				entryTable.setSelection(new StructuredSelection(entry));
				entryTable.editElement(entry, 0);
			}
		});
		removeEntryButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		removeEntryButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		removeEntryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				((WritableList) entryTable.getInput())
						.remove(((IStructuredSelection) entryTable
								.getSelection()).getFirstElement());
			}
		});
		moveEntryUpButton = toolkit.createButton(buttons, "Up", SWT.PUSH);
		moveEntryUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		moveEntryUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = entryTable.getSelection();
				int sel = entryTable.getTable().getSelectionIndex();
				TestDataEntry entry = (TestDataEntry) entryTable
						.getElementAt(sel);
				((WritableList) entryTable.getInput()).remove(entry);
				((WritableList) entryTable.getInput()).add(sel - 1, entry);
				entryTable.setSelection(selection, true);
			}
		});
		moveEntryDownButton = toolkit.createButton(buttons, "Down", SWT.PUSH);
		moveEntryDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		moveEntryDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = entryTable.getSelection();
				int sel = entryTable.getTable().getSelectionIndex();
				TestDataEntry entry = (TestDataEntry) entryTable
						.getElementAt(sel + 1);
				((WritableList) entryTable.getInput()).remove(entry);
				((WritableList) entryTable.getInput()).add(sel, entry);
				entryTable.setSelection(selection, true);
			}
		});
		toolkit.paintBordersFor(client);
		entryTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setEntryTableButtonStates();
			}
		});
	}

	private void setEntryTableButtonStates() {
		int sel = entryTable.getTable().getSelectionIndex();
		removeEntryButton.setEnabled(sel >= 0);
		moveEntryUpButton.setEnabled(sel > 0);
		moveEntryDownButton.setEnabled(sel >= 0
				&& sel < groupTable.getTable().getItemCount() - 1);
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
		ignoreChange = true;
		merge(testDataDef, getEditorInput().getTestDataDef());
		if (!documentError) {
			bindControls();
		}
		ignoreChange = false;
		lastModifyTime = 0;
		boolean readonly = documentError;
		if (!readonly) {
			try {
				readonly = getEditorInput().getStorage().isReadOnly();
			} catch (CoreException e) {
			}
		}
		UIUtils.setReadonly((Composite) getPartControl(), readonly);
		if (!readonly) {
			setGroupTableButtonStates();
			setEntryTableButtonStates();
		}
		if (!dirty) {
			createProblems();
		}
	}

	@SuppressWarnings("unchecked")
	private void merge(TestDataDef source, TestDataDef target) {
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setAuthor(source.getAuthor());
		target.setLastUpdateTime(source.getLastUpdateTime());
		if (source.getDataList() != null) {
			for (int i = 0; i < source.getDataList().size(); i++) {
				TestDataGroup group;
				if (target.getDataList().size() > i) {
					group = target.getDataList().get(i);
				} else {
					group = new TestDataGroup();
					group.setEntries(WritableList
							.withElementType(TestDataEntry.class));
					target.getDataList().add(group);
				}
				merge(source.getDataList().get(i), group);
				target.getDataList().set(i, group);
			}
			while (target.getDataList().size() > source.getDataList().size()) {
				target.getDataList().remove(target.getDataList().size() - 1);
			}
		} else {
			target.getDataList().clear();
		}
	}

	private void merge(TestDataGroup source, TestDataGroup target) {
		target.setName(source.getName());
		if (source.getEntries() != null) {
			for (int i = 0; i < source.getEntries().size(); i++) {
				TestDataEntry entry;
				if (target.getEntries().size() > i) {
					entry = target.getEntries().get(i);
				} else {
					entry = new TestDataEntry();
					target.getEntries().add(entry);
				}
				TestDataEntry e = source.getEntries().get(i);
				entry.setKey(e.getKey());
				entry.setValue(e.getValue());
				entry.setType(e.getType());
				entry.setMemo(e.getMemo());
			}
			while (target.getEntries().size() > source.getEntries().size()) {
				target.getEntries().remove(target.getEntries().size() - 1);
			}
		} else {
			target.getEntries().clear();
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
