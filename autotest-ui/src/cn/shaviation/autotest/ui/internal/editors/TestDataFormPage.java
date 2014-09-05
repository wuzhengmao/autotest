package cn.shaviation.autotest.ui.internal.editors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import cn.shaviation.autotest.core.util.Validators;
import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.model.TestDataEntry;
import cn.shaviation.autotest.model.TestDataGroup;
import cn.shaviation.autotest.model.TestDataHelper;
import cn.shaviation.autotest.ui.internal.databinding.Converters;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.util.Enums;
import cn.shaviation.autotest.util.Objects;
import cn.shaviation.autotest.util.Strings;

public class TestDataFormPage extends DocumentFormPage<TestDataDef> {

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
	private Section dataSection;
	private TableViewer entryTable;
	private Button newEntryButton;
	private Button removeEntryButton;
	private Button moveEntryUpButton;
	private Button moveEntryDownButton;

	public TestDataFormPage(TestDataEditor editor) {
		super(editor, "cn.shaviation.autotest.ui.editors.TestDataFormPage",
				"Visual Editor");
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
		dataSection.setVisible(false);
		super.createFormContent(managedForm);
	}

	private void createGeneralSection(FormToolkit toolkit, Composite container) {
		Section section = toolkit.createSection(container, SWT.HORIZONTAL
				| Section.DESCRIPTION);
		section.setText("General Information");
		section.setDescription("This section describes general information about this file.");
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
					validate(group, false);
					validateGroups();
					onFormChange();
				}
			}

			@Override
			public Object getValue(Object element, String property) {
				return Objects.toString(((TestDataGroup) element).getName());
			}

			@Override
			public boolean canModify(Object element, String property) {
				return !editor.isReadonly();
			}
		});
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		groupTable.setContentProvider(contentProvider);
		groupTable.setLabelProvider(new ObservableMapLabelProvider(
				PojoObservables.observeMap(contentProvider.getKnownElements(),
						TestDataGroup.class, "name")));
		final WritableList groupTableInput = (WritableList) getModel()
				.getDataList();
		groupTableInput.addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {

					@Override
					public void handleAdd(int index, Object element) {
						validate((TestDataGroup) element, true);
					}

					@Override
					public void handleRemove(int index, Object element) {
						clearError((TestDataGroup) element, true);
					}
				});
				validateGroups();
				onFormChange();
			}
		});
		groupTable.setInput(groupTableInput);
		Composite buttons = toolkit.createComposite(client);
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttons.setLayout(UIUtils.createButtonsGridLayout());
		newGroupButton = toolkit.createButton(buttons, "New", SWT.PUSH);
		newGroupButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
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
				| GridData.VERTICAL_ALIGN_BEGINNING));
		removeGroupButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int sel = groupTable.getTable().getSelectionIndex();
				groupTableInput.remove(sel);
			}
		});
		moveGroupUpButton = toolkit.createButton(buttons, "Up", SWT.PUSH);
		moveGroupUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		moveGroupUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = groupTable.getSelection();
				int sel = groupTable.getTable().getSelectionIndex();
				groupTableInput.add(sel - 1, groupTableInput.remove(sel));
				groupTable.setSelection(selection, true);
			}
		});
		moveGroupDownButton = toolkit.createButton(buttons, "Down", SWT.PUSH);
		moveGroupDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		moveGroupDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = groupTable.getSelection();
				int sel = groupTable.getTable().getSelectionIndex();
				groupTableInput.add(sel, groupTableInput.remove(sel + 1));
				groupTable.setSelection(selection, true);
			}
		});
		cloneGroupButton = toolkit.createButton(buttons, "Clone", SWT.PUSH);
		cloneGroupButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
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
				if (!event.getSelection().isEmpty()) {
					dataSection.setVisible(true);
					TestDataGroup group = (TestDataGroup) ((IStructuredSelection) groupTable
							.getSelection()).getFirstElement();
					entryTable.setInput(group.getEntries());
					setEntryTableButtonStates();
				} else {
					dataSection.setVisible(false);
				}
			}
		});
	}

	private void setGroupTableButtonStates() {
		if (editor.isReadonly()) {
			return;
		}
		int sel = groupTable.getTable().getSelectionIndex();
		removeGroupButton.setEnabled(sel >= 0);
		moveGroupUpButton.setEnabled(sel > 0);
		moveGroupDownButton.setEnabled(sel >= 0
				&& sel < groupTable.getTable().getItemCount() - 1);
		cloneGroupButton.setEnabled(sel >= 0);
	}

	private void validate(TestDataGroup group, boolean withChildren) {
		setError("groupTable#" + group.hashCode(),
				Validators.getErrorMessage(Validators.validateProperty(group,
						"name")));
		if (withChildren) {
			if (group.getEntries() != null) {
				validateEntries(group);
				for (TestDataEntry entry : group.getEntries()) {
					validate(entry);
				}
			}
		}
	}

	private void validate(TestDataEntry entry) {
		setError("entryTable#" + entry.hashCode(),
				Validators.getErrorMessage(Validators.validate(entry)));
	}

	private void clearError(TestDataGroup group, boolean withChildren) {
		setError("groupTable#" + group.hashCode(), null);
		if (withChildren) {
			if (group.getEntries() != null) {
				setError("testDataEntries#" + group.hashCode(), null);
				for (TestDataEntry entry : group.getEntries()) {
					clearError(entry);
				}
			}
		}
	}

	private void clearError(TestDataEntry entry) {
		setError("entryTable#" + entry.hashCode(), null);
	}

	private void validateGroups() {
		setError("testDataGroups", Validators.getErrorMessage(Validators
				.validateProperty(getModel(), "dataList")));
	}

	private void validateEntries(TestDataGroup group) {
		setError("testDataEntries#" + group.hashCode(),
				Validators.getErrorMessage(Validators.validateProperty(group,
						"entries")));
	}

	private void createDataSection(FormToolkit toolkit, Composite container) {
		dataSection = toolkit.createSection(container, SWT.HORIZONTAL
				| Section.DESCRIPTION);
		dataSection.setText("Test Data Specification");
		dataSection
				.setDescription("Specify the specification of current selected test data group.");
		dataSection.setLayoutData(new GridData(GridData.FILL_BOTH));
		dataSection.setLayout(UIUtils.createClearTableWrapLayout(false, 1));
		Composite client = toolkit.createComposite(dataSection);
		client.setLayout(UIUtils.createSectionClientGridLayout(false, 2));
		dataSection.setClient(client);
		entryTable = new TableViewer(client, SWT.HIDE_SELECTION
				| SWT.FULL_SELECTION | SWT.V_SCROLL | toolkit.getBorderStyle());
		entryTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		entryTable.getTable().setHeaderVisible(true);
		entryTable.getTable().setLinesVisible(true);
		toolkit.adapt(entryTable.getTable(), false, false);
		final TableViewerColumn tvc1 = new TableViewerColumn(entryTable,
				SWT.LEFT);
		tvc1.getColumn().setWidth(60);
		tvc1.getColumn().setText("Key");
		final TableViewerColumn tvc2 = new TableViewerColumn(entryTable,
				SWT.LEFT);
		tvc2.getColumn().setWidth(120);
		tvc2.getColumn().setText("Value");
		final TableViewerColumn tvc3 = new TableViewerColumn(entryTable,
				SWT.LEFT);
		tvc3.getColumn().setWidth(60);
		tvc3.getColumn().setText("Type");
		final TableViewerColumn tvc4 = new TableViewerColumn(entryTable,
				SWT.LEFT);
		tvc4.getColumn().setText("Memo");
		entryTable.getTable().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent event) {
				Point size = ((Control) event.getSource()).getSize();
				int w = size.x - 66;
				ScrollBar vbar = ((Scrollable) event.getSource())
						.getVerticalBar();
				if (vbar != null && vbar.isVisible()) {
					w -= vbar.getSize().x;
				}
				int t = w / 5;
				if (t > 60) {
					tvc1.getColumn().setWidth(t);
					tvc2.getColumn().setWidth(t * 2);
					tvc4.getColumn().setWidth(w - t * 3);
				} else {
					tvc1.getColumn().setWidth(60);
					tvc2.getColumn().setWidth(120);
					tvc4.getColumn().setWidth(120);
				}
			}
		});
		final IListChangeListener listChangeListener = new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {

					@Override
					public void handleAdd(int index, Object element) {
						validate((TestDataEntry) element);
					}

					@Override
					public void handleRemove(int index, Object element) {
						clearError((TestDataEntry) element);
					}
				});
				validateEntries((TestDataGroup) groupTable
						.getElementAt(groupTable.getTable().getSelectionIndex()));
				onFormChange();
			}
		};
		ObservableListContentProvider contentProvider = new ObservableListContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				super.inputChanged(viewer, oldInput, newInput);
				if (oldInput != null) {
					((WritableList) oldInput)
							.removeListChangeListener(listChangeListener);
				}
				if (newInput != null) {
					((WritableList) newInput)
							.addListChangeListener(listChangeListener);
				}
			}
		};
		entryTable.setContentProvider(contentProvider);
		final String[] propertyNames = new String[] { "key", "value", "type",
				"memo" };
		final IObservableMap[] observableMaps = PojoObservables.observeMaps(
				contentProvider.getKnownElements(), TestDataEntry.class,
				propertyNames);
		entryTable.setLabelProvider(new ObservableMapLabelProvider(
				observableMaps));
		ComboBoxCellEditor cbce = new ComboBoxCellEditor(entryTable.getTable(),
				Enums.getNames(TestDataEntry.Type.class), SWT.LEFT
						| SWT.READ_ONLY);
		cbce.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION
				| ComboBoxViewerCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION);
		entryTable.setCellEditors(new CellEditor[] {
				new TextCellEditor(entryTable.getTable(), SWT.LEFT),
				new TextCellEditor(entryTable.getTable(), SWT.LEFT), cbce,
				new TextCellEditor(entryTable.getTable(), SWT.LEFT) });
		entryTable.setColumnProperties(propertyNames);
		entryTable.setCellModifier(new ICellModifier() {

			@Override
			public void modify(Object element, String property, Object value) {
				TestDataEntry entry = (TestDataEntry) ((Item) element)
						.getData();
				Object oldValue = observableMaps[getIndex(property)].get(entry);
				if ("type".equals(property)) {
					value = Enums.getEnum(TestDataEntry.Type.class,
							(Integer) value);
				}
				if (!Strings.equals(oldValue, value)) {
					observableMaps[getIndex(property)].put(entry, value);
					entryTable.update(entry, new String[] { property });
					validate(entry);
					validateEntries((TestDataGroup) groupTable
							.getElementAt(groupTable.getTable()
									.getSelectionIndex()));
					onFormChange();
				}
			}

			@Override
			public Object getValue(Object element, String property) {
				Object value = observableMaps[getIndex(property)].get(element);
				if ("type".equals(property)) {
					return value != null ? ((TestDataEntry.Type) value)
							.ordinal() : -1;
				} else {
					return Objects.toString(value);
				}
			}

			private int getIndex(String property) {
				for (int i = 0; i < propertyNames.length; i++) {
					if (property.equals(propertyNames[i])) {
						return i;
					}
				}
				return -1;
			}

			@Override
			public boolean canModify(Object element, String property) {
				return !editor.isReadonly();
			}
		});
		Composite buttons = toolkit.createComposite(client);
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttons.setLayout(UIUtils.createButtonsGridLayout());
		newEntryButton = toolkit.createButton(buttons, "New", SWT.PUSH);
		newEntryButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
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
				| GridData.VERTICAL_ALIGN_BEGINNING));
		removeEntryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int sel = entryTable.getTable().getSelectionIndex();
				((WritableList) entryTable.getInput()).remove(sel);
			}
		});
		moveEntryUpButton = toolkit.createButton(buttons, "Up", SWT.PUSH);
		moveEntryUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		moveEntryUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = entryTable.getSelection();
				int sel = entryTable.getTable().getSelectionIndex();
				((WritableList) entryTable.getInput()).add(sel - 1,
						((WritableList) entryTable.getInput()).remove(sel));
				entryTable.setSelection(selection, true);
			}
		});
		moveEntryDownButton = toolkit.createButton(buttons, "Down", SWT.PUSH);
		moveEntryDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		moveEntryDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = entryTable.getSelection();
				int sel = entryTable.getTable().getSelectionIndex();
				((WritableList) entryTable.getInput()).add(sel,
						((WritableList) entryTable.getInput()).remove(sel + 1));
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
		if (editor.isReadonly()) {
			return;
		}
		int sel = entryTable.getTable().getSelectionIndex();
		removeEntryButton.setEnabled(sel >= 0);
		moveEntryUpButton.setEnabled(sel > 0);
		moveEntryDownButton.setEnabled(sel >= 0
				&& sel < entryTable.getTable().getItemCount() - 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected TestDataDef createModel() {
		TestDataDef testDataDef = new TestDataDef();
		testDataDef.setDataList(WritableList
				.withElementType(TestDataGroup.class));
		return testDataDef;
	}

	@Override
	protected void initModel(TestDataDef model) {
		super.initModel(model);
		model.setAuthor(System.getProperty("user.name"));
	}

	@Override
	protected TestDataDef convertSourceToModel(String source) throws Exception {
		return TestDataHelper.parse(source);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void mergeModel(TestDataDef source, TestDataDef target) {
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
				}
				merge(source.getDataList().get(i), group);
				if (target.getDataList().size() > i) {
					target.getDataList().set(i, group);
				} else {
					target.getDataList().add(group);
				}
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
				}
				TestDataEntry e = source.getEntries().get(i);
				entry.setKey(e.getKey());
				entry.setValue(e.getValue());
				entry.setType(e.getType());
				entry.setMemo(e.getMemo());
				if (target.getEntries().size() > i) {
					target.getEntries().set(i, entry);
				} else {
					target.getEntries().add(entry);
				}
			}
			while (target.getEntries().size() > source.getEntries().size()) {
				target.getEntries().remove(target.getEntries().size() - 1);
			}
		} else {
			target.getEntries().clear();
		}
	}

	@Override
	protected void enableControls(boolean readonly) {
		super.enableControls(readonly);
		if (!readonly) {
			setGroupTableButtonStates();
			if (dataSection.isVisible()) {
				setEntryTableButtonStates();
			}
		}
	}

	@Override
	protected void bindControls(DataBindingContext dataBindingContext,
			TestDataDef model) {
		IManagedForm managedForm = getManagedForm();
		UIUtils.bindText(dataBindingContext, managedForm, nameText, model,
				"name", Converters.TRIM, Converters.TRIM);
		UIUtils.bindText(dataBindingContext, managedForm, descText, model,
				"description", Converters.TRIM, Converters.TRIM);
		UIUtils.bindText(dataBindingContext, managedForm, authorText, model,
				"author", Converters.TRIM, Converters.TRIM);
		UIUtils.bindText(dataBindingContext, managedForm, modifyTime, model,
				"lastUpdateTime", null, Converters.DATESTAMP);
	}

	@Override
	protected void postLoadModel(TestDataDef model) {
		super.postLoadModel(model);
		if (groupTable.getSelection().isEmpty()
				&& !model.getDataList().isEmpty()) {
			groupTable.setSelection(new StructuredSelection(model.getDataList()
					.get(0)), true);
		}
	}
}
