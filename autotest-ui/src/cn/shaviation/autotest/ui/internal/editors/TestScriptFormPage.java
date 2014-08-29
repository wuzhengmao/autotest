package cn.shaviation.autotest.ui.internal.editors;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import cn.shaviation.autotest.core.jdt.AutoTestProjects;
import cn.shaviation.autotest.core.model.Parameter;
import cn.shaviation.autotest.core.model.TestDataEntry;
import cn.shaviation.autotest.core.model.TestDataGroup;
import cn.shaviation.autotest.core.model.TestScript;
import cn.shaviation.autotest.core.model.TestScriptHelper;
import cn.shaviation.autotest.core.model.TestStep;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.core.util.Strings;
import cn.shaviation.autotest.core.util.Validators;
import cn.shaviation.autotest.ui.internal.databinding.Converters;
import cn.shaviation.autotest.ui.internal.util.TableLabelProvider;
import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class TestScriptFormPage extends DocumentFormPage<TestScript> {

	private IJavaProject javaProject;
	
	private Text nameText;
	private Text descText;
	private Text authorText;
	private Label modifyTime;
	private TableViewer stepTable;
	private Button newStepButton;
	private Button removeStepButton;
	private Button moveStepUpButton;
	private Button moveStepDownButton;

	private TableViewer entryTable;
	private Button newEntryButton;
	private Button removeEntryButton;
	private Button moveEntryUpButton;
	private Button moveEntryDownButton;

	public TestScriptFormPage(TestScriptEditor editor) {
		super(editor, "cn.shaviation.autotest.ui.editors.TestScriptFormPage",
				"Visual Editor");
	}

	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		IProject project = getEditorInput().getFile().getProject();
		javaProject = JavaUtils.getJavaProject(project);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.setText("Test Script Editor");
		Composite body = form.getBody();
		body.setLayout(UIUtils.createFormGridLayout(true, 2));
		Composite leftComposite = toolkit.createComposite(body, SWT.NONE);
		leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		leftComposite.setLayout(UIUtils.createFormPaneGridLayout(false, 1));
		createGeneralSection(toolkit, leftComposite);
		createStepsSection(toolkit, leftComposite);
		Composite rightComposite = toolkit.createComposite(body, SWT.NONE);
		rightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		rightComposite.setLayout(UIUtils.createFormPaneGridLayout(false, 1));
		createDataSection(toolkit, rightComposite);
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

	private void createStepsSection(FormToolkit toolkit, Composite container) {
		Section section = toolkit.createSection(container, SWT.HORIZONTAL
				| Section.DESCRIPTION);
		section.setText("Test Steps");
		section.setDescription("Specify all the test steps of this script, the upper step will be excuted early.");
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		section.setLayout(UIUtils.createClearGridLayout(false, 1));
		Composite client = toolkit.createComposite(section);
		client.setLayout(UIUtils.createSectionClientGridLayout(false, 2));
		section.setClient(client);
		stepTable = new TableViewer(client, SWT.FULL_SELECTION | SWT.V_SCROLL
				| toolkit.getBorderStyle());
		stepTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		stepTable.getTable().setHeaderVisible(true);
		stepTable.getTable().setLinesVisible(true);
		toolkit.adapt(stepTable.getTable(), false, false);
		final TableViewerColumn tvc1 = new TableViewerColumn(stepTable,
				SWT.RIGHT);
		tvc1.getColumn().setWidth(15);
		tvc1.getColumn().setText("No.");
		final TableViewerColumn tvc2 = new TableViewerColumn(stepTable,
				SWT.LEFT);
		tvc2.getColumn().setWidth(60);
		tvc2.getColumn().setText("Name");
		final TableViewerColumn tvc3 = new TableViewerColumn(stepTable,
				SWT.LEFT);
		tvc3.getColumn().setWidth(15);
		tvc3.getColumn().setText("Type");
		final TableViewerColumn tvc4 = new TableViewerColumn(stepTable,
				SWT.LEFT);
		tvc4.getColumn().setWidth(60);
		tvc4.getColumn().setText("Prev.");
		stepTable.getTable().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent event) {
				Point size = ((Control) event.getSource()).getSize();
				int w = size.x - 96;
				tvc2.getColumn().setWidth(w > 60 ? w : 60);
			}
		});
		final WritableList testSteps = (WritableList) getEditorInput()
				.getModel().getTestSteps();
		testSteps.addListChangeListener(new IListChangeListener() {
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
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		stepTable.setContentProvider(contentProvider);
		stepTable.setLabelProvider(new TableLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				TestStep step = (TestStep) element;
				switch (columnIndex) {
				case 0:
					return String.valueOf(testSteps.indexOf(element) + 1);
				case 1:
					return getTestStepName(step);
				case 2:
					if (step.getInvokeType() == TestStep.Type.TestMethod) {
						return "M";
					} else if (step.getInvokeType() == TestStep.Type.TestScript) {
						return "S";
					}
				case 3:
					if (step.getDependentSteps() != null && !step.getDependentSteps().isEmpty()) {
						return Strings.merge(step.getDependentSteps(), ",");
					}
				}
				return "";
			}
		});
		stepTable.setInput(testSteps);
		Composite buttons = toolkit.createComposite(client);
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttons.setLayout(UIUtils.createButtonsGridLayout());
		newStepButton = toolkit.createButton(buttons, "New", SWT.PUSH);
		newStepButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		newStepButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				TestStep step = new TestStep();
				step.setInvokeType(TestStep.Type.TestMethod);
				step.setParameters(WritableList
						.withElementType(Parameter.class));
				testSteps.add(step);
				stepTable.setSelection(new StructuredSelection(step));
			}
		});
		removeStepButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		removeStepButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		removeStepButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				int sel = stepTable.getTable().getSelectionIndex();
				stepTable.remove(sel);
				removeDependence(testSteps, sel);
			}
		});
		moveStepUpButton = toolkit.createButton(buttons, "Up", SWT.PUSH);
		moveStepUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		moveStepUpButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = stepTable.getSelection();
				int sel = stepTable.getTable().getSelectionIndex();
				testSteps.add(sel - 1, testSteps.remove(sel));
				stepTable.setSelection(selection, true);
				exchangeDependence(testSteps, sel, sel - 1);
			}
		});
		moveStepDownButton = toolkit.createButton(buttons, "Down", SWT.PUSH);
		moveStepDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.CENTER));
		moveStepDownButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = stepTable.getSelection();
				int sel = stepTable.getTable().getSelectionIndex();
				testSteps.add(sel, testSteps.remove(sel + 1));
				stepTable.setSelection(selection, true);
				exchangeDependence(testSteps, sel, sel + 1);
			}
		});
		toolkit.paintBordersFor(client);
		stepTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setStepTableButtonStates();
				setEntryTableButtonStates();
				if (!event.getSelection().isEmpty()) {
					TestStep step = (TestStep) ((IStructuredSelection) stepTable
							.getSelection()).getFirstElement();
					entryTable.setInput(group.getEntries());
				} else {
					entryTable.setInput(WritableList
							.withElementType(TestDataEntry.class));
				}
			}
		});
	}
	
	private String getTestStepName(TestStep step) {
		if (javaProject != null && !Strings.isEmpty(step.getInvokeTarget())) {
			if (step.getInvokeType() == TestStep.Type.TestMethod) {
				return AutoTestProjects.getTestMethodName(javaProject, step.getInvokeTarget());
			} else if (step.getInvokeType() == TestStep.Type.TestMethod) {
				return AutoTestProjects.getTestScriptName(javaProject, step.getInvokeTarget());
			}
		}
		return "";
	}
	
	private void exchangeDependence(List<TestStep> testSteps, Integer i, Integer j) {
		for (int l = 0; l < testSteps.size(); l++) {
			TestStep step = testSteps.get(l);
			List<Integer> dependence = step.getDependentSteps();
			if (dependence != null && !dependence.isEmpty()) {
				boolean changed = false;
				for (int k = 0; k < dependence.size(); k++) {
					if (dependence.get(k) == i) {
						dependence.set(k, j);
						changed = true;
					} else if (dependence.get(k) == j) {
						dependence.set(k, i);
						changed = true;
					}
				}
				if (changed) {
					Collections.sort(dependence);
					testSteps.set(l, step);
				}
			}
		}
	}
	
	private void removeDependence(List<TestStep> testSteps, Integer i) {
		for (int l = 0; l < testSteps.size(); l++) {
			TestStep step = testSteps.get(l);
			List<Integer> dependence = step.getDependentSteps();
			if (dependence != null && !dependence.isEmpty()) {
				boolean changed = false;
				for (int k = dependence.size()-1; k >= 0; k--) {
					if (dependence.get(k) == i) {
						dependence.remove(k);
						changed = true;
					}
				}
				if (changed) {
					testSteps.set(l, step);
				}
			}
		}
	}

	private void setStepTableButtonStates() {
		int sel = stepTable.getTable().getSelectionIndex();
		removeStepButton.setEnabled(sel >= 0);
		moveStepUpButton.setEnabled(sel > 0);
		moveStepDownButton.setEnabled(sel >= 0
				&& sel < stepTable.getTable().getItemCount() - 1);
	}

	private void validate(TestStep step, boolean withChildren) {
		setError("stepTable#" + step.hashCode(),
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
				.validateProperty(getEditorInput().getModel(), "dataList")));
	}

	private void validateEntries(TestDataGroup group) {
		setError("testDataEntries#" + group.hashCode(),
				Validators.getErrorMessage(Validators.validateProperty(group,
						"entries")));
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
		final TestDataEntry.Type[] types = TestDataEntry.Type.values();
		String[] typeNames = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			typeNames[i] = types[i].name();
		}
		ComboBoxCellEditor cbce = new ComboBoxCellEditor(entryTable.getTable(),
				typeNames, SWT.LEFT | SWT.READ_ONLY);
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
					value = types[(Integer) value];
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
					return Strings.objToString(value);
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
				return true;
			}
		});
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
				int sel = entryTable.getTable().getSelectionIndex();
				((WritableList) entryTable.getInput()).remove(sel);
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
				((WritableList) entryTable.getInput()).add(sel - 1,
						((WritableList) entryTable.getInput()).remove(sel));
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
		boolean gsel = !groupTable.getSelection().isEmpty();
		int sel = entryTable.getTable().getSelectionIndex();
		newEntryButton.setEnabled(gsel);
		removeEntryButton.setEnabled(gsel && sel >= 0);
		moveEntryUpButton.setEnabled(gsel && sel > 0);
		moveEntryDownButton.setEnabled(gsel && sel >= 0
				&& sel < groupTable.getTable().getItemCount() - 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected TestScript createModel() {
		TestScript testScript = new TestScript();
		testScript.setTestSteps(WritableList.withElementType(TestStep.class));
		return testScript;
	}

	@Override
	protected void initModel(TestScript model) {
		super.initModel(model);
		model.setAuthor(System.getProperty("user.name"));
	}

	@Override
	protected TestScript convertSourceToModel(String source) throws Exception {
		return TestScriptHelper.parse(source);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void mergeModel(TestScript source, TestScript target) {
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setAuthor(source.getAuthor());
		target.setLastUpdateTime(source.getLastUpdateTime());
		if (source.getTestSteps() != null) {
			for (int i = 0; i < source.getTestSteps().size(); i++) {
				TestStep step;
				if (target.getTestSteps().size() > i) {
					step = target.getTestSteps().get(i);
				} else {
					step = new TestStep();
					step.setParameters(WritableList
							.withElementType(Parameter.class));
				}
				merge(source.getTestSteps().get(i), step);
				if (target.getTestSteps().size() > i) {
					target.getTestSteps().set(i, step);
				} else {
					target.getTestSteps().add(step);
				}
			}
			while (target.getTestSteps().size() > source.getTestSteps().size()) {
				target.getTestSteps().remove(target.getTestSteps().size() - 1);
			}
		} else {
			target.getTestSteps().clear();
		}
	}

	private void merge(TestStep source, TestStep target) {
		target.setDependentSteps(source.getDependentSteps());
		target.setInvokeType(source.getInvokeType());
		target.setInvokeTarget(source.getInvokeTarget());
		target.setTestDataFile(source.getTestDataFile());
		target.setLoopTimes(source.getLoopTimes());
		if (source.getParameters() != null) {
			for (int i = 0; i < source.getParameters().size(); i++) {
				Parameter param;
				if (target.getParameters().size() > i) {
					param = target.getParameters().get(i);
				} else {
					param = new Parameter();
				}
				Parameter e = source.getParameters().get(i);
				param.setKey(e.getKey());
				param.setValue(e.getValue());
				param.setMemo(e.getMemo());
				if (target.getParameters().size() > i) {
					target.getParameters().set(i, param);
				} else {
					target.getParameters().add(param);
				}
			}
			while (target.getParameters().size() > source.getParameters()
					.size()) {
				target.getParameters()
						.remove(target.getParameters().size() - 1);
			}
		} else {
			target.getParameters().clear();
		}
	}

	@Override
	protected void enableControls(boolean readonly) {
		super.enableControls(readonly);
		if (!readonly) {
			setStepTableButtonStates();
			setEntryTableButtonStates();
		}
	}

	@Override
	protected void bindControls(TestScript model) {
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
}
