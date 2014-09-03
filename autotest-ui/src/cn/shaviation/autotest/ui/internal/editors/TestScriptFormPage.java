package cn.shaviation.autotest.ui.internal.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboViewer;
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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import cn.shaviation.autotest.core.jdt.AutoTestProjects;
import cn.shaviation.autotest.core.model.Parameter;
import cn.shaviation.autotest.core.model.TestScript;
import cn.shaviation.autotest.core.model.TestScriptHelper;
import cn.shaviation.autotest.core.model.TestStep;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.core.util.Objects;
import cn.shaviation.autotest.core.util.Strings;
import cn.shaviation.autotest.core.util.Validators;
import cn.shaviation.autotest.ui.internal.databinding.Converters;
import cn.shaviation.autotest.ui.internal.databinding.ListToStringConverter;
import cn.shaviation.autotest.ui.internal.databinding.StringToListConverter;
import cn.shaviation.autotest.ui.internal.dialogs.TestDataSelectionDialog;
import cn.shaviation.autotest.ui.internal.dialogs.TestMethodSelectionDialog;
import cn.shaviation.autotest.ui.internal.dialogs.TestScriptSelectionDialog;
import cn.shaviation.autotest.ui.internal.util.EnumLabelProvider;
import cn.shaviation.autotest.ui.internal.util.NumberVerifyListener;
import cn.shaviation.autotest.ui.internal.util.SelectionChangedListener;
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

	private DataBindingContext dataBindingContext;
	private Section detailSection;
	private ComboViewer invokeTypeCombo;
	private Label invokeTargetLabel;
	private Text invokeTargetText;
	private Button invokeTargetButton;
	private Label testDataLabel;
	private Text testDataText;
	private Button testDataButton;
	private Text loopTimesText;
	private Text dependenceText;
	private TableViewer paramTable;
	private Button newParamButton;
	private Button removeParamButton;
	private Button moveParamUpButton;
	private Button moveParamDownButton;

	private static final Pattern DEPENDENCE_PATTERN = Pattern
			.compile("^[0-9,]*$");
	private static VerifyListener dependenceVerifyListener = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent event) {
			event.doit = DEPENDENCE_PATTERN.matcher(event.text).matches();
		}
	};

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
		createDetailSection(toolkit, rightComposite);
		detailSection.setVisible(false);
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
		tvc1.getColumn().setWidth(35);
		tvc1.getColumn().setText("No.");
		final TableViewerColumn tvc2 = new TableViewerColumn(stepTable,
				SWT.LEFT);
		tvc2.getColumn().setWidth(80);
		tvc2.getColumn().setText("Name");
		final TableViewerColumn tvc3 = new TableViewerColumn(stepTable,
				SWT.LEFT);
		tvc3.getColumn().setWidth(45);
		tvc3.getColumn().setText("Type");
		final TableViewerColumn tvc4 = new TableViewerColumn(stepTable,
				SWT.LEFT);
		tvc4.getColumn().setWidth(80);
		tvc4.getColumn().setText("Prev.");
		stepTable.getTable().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent event) {
				Point size = ((Control) event.getSource()).getSize();
				int w = size.x - 166;
				ScrollBar vbar = ((Scrollable) event.getSource())
						.getVerticalBar();
				if (vbar != null && vbar.isVisible()) {
					w -= vbar.getSize().x;
				}
				tvc2.getColumn().setWidth(w > 80 ? w : 80);
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
						if (index != stepTable.getTable().getSelectionIndex()) {
							validate((TestStep) element, true);
						}
					}

					@Override
					public void handleRemove(int index, Object element) {
						if (index != stepTable.getTable().getSelectionIndex()) {
							clearError((TestStep) element, true);
						}
					}
				});
				onFormChange();
			}
		});
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		stepTable.setContentProvider(contentProvider);
		stepTable.setLabelProvider(new ObservableMapLabelProvider(
				BeansObservables.observeMaps(
						contentProvider.getKnownElements(), TestStep.class,
						new String[] { "invokeTarget", "invokeType",
								"dependentSteps" })) {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				TestStep step = (TestStep) element;
				switch (columnIndex) {
				case 0:
					return String.valueOf(testSteps.indexOf(element) + 1);
				case 1:
					return getTestStepName(step);
				case 2:
					if (step.getInvokeType() == TestStep.Type.Method) {
						return "M";
					} else if (step.getInvokeType() == TestStep.Type.Script) {
						return "S";
					}
				case 3:
					if (step.getDependentSteps() != null
							&& !step.getDependentSteps().isEmpty()) {
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
				| GridData.VERTICAL_ALIGN_BEGINNING));
		newStepButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				TestStep step = new TestStep();
				step.setInvokeType(TestStep.Type.Method);
				step.setLoopTimes(1);
				step.setParameters(WritableList
						.withElementType(Parameter.class));
				testSteps.add(step);
				stepTable.setSelection(new StructuredSelection(step));
			}
		});
		removeStepButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		removeStepButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		removeStepButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				int sel = stepTable.getTable().getSelectionIndex();
				stepTable.setSelection(StructuredSelection.EMPTY);
				testSteps.remove(sel);
				reorderStepTable(sel);
				removeDependence(testSteps, sel + 1);
				validateDependence();
			}
		});
		moveStepUpButton = toolkit.createButton(buttons, "Up", SWT.PUSH);
		moveStepUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		moveStepUpButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = stepTable.getSelection();
				int sel = stepTable.getTable().getSelectionIndex();
				testSteps.add(sel - 1, testSteps.remove(sel));
				reorderStepTable(sel - 1);
				stepTable.setSelection(selection, true);
				exchangeDependence(testSteps, sel + 1, sel);
			}
		});
		moveStepDownButton = toolkit.createButton(buttons, "Down", SWT.PUSH);
		moveStepDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		moveStepDownButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = stepTable.getSelection();
				int sel = stepTable.getTable().getSelectionIndex();
				testSteps.add(sel, testSteps.remove(sel + 1));
				reorderStepTable(sel);
				stepTable.setSelection(selection, true);
				exchangeDependence(testSteps, sel + 1, sel + 2);
			}
		});
		toolkit.paintBordersFor(client);
		stepTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setStepTableButtonStates();
				if (!event.getSelection().isEmpty()) {
					TestStep step = (TestStep) ((IStructuredSelection) stepTable
							.getSelection()).getFirstElement();
					if (!step.equals(detailSection.getData())) {
						unbindDetails((TestStep) detailSection.getData());
						detailSection.setVisible(true);
						detailSection.setData(step);
						setIgnoreChange(true);
						bindDetails(step);
						setIgnoreChange(false);
						setParamTableButtonStates();
					}
				} else {
					unbindDetails((TestStep) detailSection.getData());
					detailSection.setVisible(false);
					detailSection.setData(null);
				}
			}
		});
	}

	private String getTestStepName(TestStep step) {
		if (javaProject != null && !Strings.isEmpty(step.getInvokeTarget())) {
			if (step.getInvokeType() == TestStep.Type.Method) {
				return AutoTestProjects.getTestMethodName(javaProject,
						step.getInvokeTarget());
			} else if (step.getInvokeType() == TestStep.Type.Script) {
				return AutoTestProjects.getTestScriptName(javaProject,
						step.getInvokeTarget());
			}
		}
		return "";
	}

	private void reorderStepTable(int index) {
		for (int i = index; i < stepTable.getTable().getItemCount(); i++) {
			stepTable.getTable().getItem(i).setText(0, String.valueOf(i + 1));
		}
	}

	private void exchangeDependence(List<TestStep> testSteps, Integer i,
			Integer j) {
		for (int l = 0; l < testSteps.size(); l++) {
			TestStep step = testSteps.get(l);
			List<Integer> dependence = step.getDependentSteps();
			if (dependence != null && !dependence.isEmpty()) {
				dependence = new ArrayList<Integer>(dependence);
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
					step.setDependentSteps(dependence);
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
				for (int k = dependence.size() - 1; k >= 0; k--) {
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

	private void validateDependence() {
		setError("testScript.testSteps", Validators.getErrorMessage(Validators
				.validateProperty(getEditorInput().getModel(), "testSteps")));
	}

	private void validate(TestStep step, boolean withChildren) {
		setError("stepTable.invokeType#" + step.hashCode(),
				Validators.getErrorMessage(Validators.validateProperty(step,
						"invokeType")));
		setError("stepTable.invokeTarget#" + step.hashCode(),
				Validators.getErrorMessage(Validators.validateProperty(step,
						"invokeTarget")));
		setError("stepTable.loopTimes#" + step.hashCode(),
				Validators.getErrorMessage(Validators.validateProperty(step,
						"loopTimes")));
		if (withChildren) {
			if (step.getParameters() != null) {
				validateParameters(step);
				for (Parameter param : step.getParameters()) {
					validate(param);
				}
			}
		}
	}

	private void validate(Parameter param) {
		setError("stepTable.parameters.p#" + param.hashCode(),
				Validators.getErrorMessage(Validators.validate(param)));
	}

	private void clearError(TestStep step, boolean withChildren) {
		setError("stepTable.invokeType#" + step.hashCode(), null);
		setError("stepTable.invokeTarget#" + step.hashCode(), null);
		setError("stepTable.loopTimes#" + step.hashCode(), null);
		if (withChildren) {
			if (step.getParameters() != null) {
				setError("stepTable.parameters#" + step.hashCode(), null);
				for (Parameter param : step.getParameters()) {
					clearError(param);
				}
			}
		}
	}

	private void clearError(Parameter param) {
		setError("stepTable.parameters.p#" + param.hashCode(), null);
	}

	private void validateParameters(TestStep step) {
		setError("stepTable.parameters#" + step.hashCode(),
				Validators.getErrorMessage(Validators.validateProperty(step,
						"parameters")));
	}

	private void createDetailSection(FormToolkit toolkit, Composite container) {
		detailSection = toolkit.createSection(container, SWT.HORIZONTAL
				| Section.DESCRIPTION);
		detailSection.setText("Test Step Details");
		detailSection
				.setDescription("Specify the properties and runtime parameters of the selected test step.");
		detailSection.setLayoutData(new GridData(GridData.FILL_BOTH));
		detailSection.setLayout(UIUtils.createClearTableWrapLayout(false, 1));
		Composite client = toolkit.createComposite(detailSection);
		client.setLayout(UIUtils.createSectionClientGridLayout(false, 3));
		detailSection.setClient(client);
		toolkit.createLabel(client, "Type:").setLayoutData(new GridData());
		invokeTypeCombo = new ComboViewer(client, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd_invokeTypeCombo = new GridData();
		gd_invokeTypeCombo.horizontalIndent = 5;
		invokeTypeCombo.getCombo().setLayoutData(gd_invokeTypeCombo);
		toolkit.adapt(invokeTypeCombo.getCombo(), false, false);
		invokeTypeCombo
				.addSelectionChangedListener(new SelectionChangedListener() {
					@Override
					public void onSelectionChanged(SelectionChangedEvent event) {
						onFormChange();
						TestStep.Type type = event.getSelection().isEmpty() ? null
								: (TestStep.Type) ((IStructuredSelection) event
										.getSelection()).getFirstElement();
						if (type == TestStep.Type.Method) {
							invokeTargetLabel.setText("Test Method:");
							invokeTargetLabel.setVisible(true);
							invokeTargetText.setText("");
							invokeTargetText.setVisible(true);
							invokeTargetButton.setVisible(true);
							testDataLabel.setVisible(true);
							testDataText.setText("");
							testDataText.setVisible(true);
							testDataButton.setVisible(true);
						} else if (type == TestStep.Type.Script) {
							invokeTargetLabel.setText("Test Script:");
							invokeTargetLabel.setVisible(true);
							invokeTargetText.setText("");
							invokeTargetText.setVisible(true);
							invokeTargetButton.setVisible(true);
							testDataLabel.setVisible(false);
							testDataText.setText("");
							testDataText.setVisible(false);
							testDataButton.setVisible(false);
						} else {
							invokeTargetLabel.setVisible(false);
							invokeTargetText.setText("");
							invokeTargetText.setVisible(false);
							invokeTargetButton.setVisible(false);
							testDataLabel.setVisible(false);
							testDataText.setText("");
							testDataText.setVisible(false);
							testDataButton.setVisible(false);
						}
					}
				});
		invokeTypeCombo.setContentProvider(new ArrayContentProvider());
		invokeTypeCombo.setLabelProvider(new EnumLabelProvider());
		invokeTypeCombo.setInput(TestStep.Type.values());
		toolkit.createLabel(client, "").setLayoutData(new GridData());
		invokeTargetLabel = toolkit.createLabel(client, "Test Method:");
		invokeTargetLabel.setLayoutData(new GridData());
		invokeTargetLabel.setVisible(false);
		invokeTargetText = toolkit.createText(client, "", SWT.READ_ONLY);
		GridData gd_invokeTargetText = new GridData(GridData.FILL_HORIZONTAL);
		gd_invokeTargetText.horizontalIndent = 5;
		invokeTargetText.setLayoutData(gd_invokeTargetText);
		invokeTargetText.setVisible(false);
		invokeTargetText.addModifyListener(defaultModifyListener);
		invokeTargetButton = toolkit
				.createButton(client, "Select...", SWT.PUSH);
		invokeTargetButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL));
		invokeTargetButton.setVisible(false);
		invokeTargetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (testDataText.isVisible()) {
					selectTestMethod();
				} else {
					selectTestScript();
				}
			}
		});
		testDataLabel = toolkit.createLabel(client, "Test Data:");
		testDataLabel.setLayoutData(new GridData());
		testDataLabel.setVisible(false);
		testDataText = toolkit.createText(client, "", SWT.READ_ONLY);
		GridData gd_testDataText = new GridData(GridData.FILL_HORIZONTAL);
		gd_testDataText.horizontalIndent = 5;
		testDataText.setLayoutData(gd_testDataText);
		testDataText.setVisible(false);
		testDataText.addModifyListener(defaultModifyListener);
		testDataButton = toolkit.createButton(client, "Select...", SWT.PUSH);
		testDataButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL));
		testDataButton.setVisible(false);
		testDataButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				selectTestData();
			}
		});
		toolkit.createLabel(client, "Loops:").setLayoutData(new GridData());
		loopTimesText = toolkit.createText(client, "1", SWT.RIGHT);
		GridData gd_loopTimesText = new GridData();
		gd_loopTimesText.horizontalIndent = 5;
		gd_loopTimesText.widthHint = 40;
		loopTimesText.setLayoutData(gd_loopTimesText);
		loopTimesText.addModifyListener(defaultModifyListener);
		loopTimesText.addVerifyListener(new NumberVerifyListener());
		toolkit.createLabel(client, "").setLayoutData(new GridData());
		toolkit.createLabel(client, "Dependence:")
				.setLayoutData(new GridData());
		dependenceText = toolkit.createText(client, "");
		GridData gd_dependenceText = new GridData(GridData.FILL_HORIZONTAL);
		gd_dependenceText.horizontalIndent = 5;
		dependenceText.setLayoutData(gd_dependenceText);
		dependenceText.addModifyListener(defaultModifyListener);
		dependenceText.addVerifyListener(dependenceVerifyListener);
		toolkit.createLabel(client, "").setLayoutData(new GridData());
		toolkit.createLabel(client, "Runtime Parameters:").setLayoutData(
				new GridData(GridData.BEGINNING, GridData.CENTER, false, false,
						3, 1));
		paramTable = new TableViewer(client, SWT.HIDE_SELECTION
				| SWT.FULL_SELECTION | SWT.V_SCROLL | toolkit.getBorderStyle());
		paramTable.getTable().setLayoutData(
				new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
		paramTable.getTable().setHeaderVisible(true);
		paramTable.getTable().setLinesVisible(true);
		toolkit.adapt(paramTable.getTable(), false, false);
		final TableViewerColumn tvc1 = new TableViewerColumn(paramTable,
				SWT.LEFT);
		tvc1.getColumn().setWidth(60);
		tvc1.getColumn().setText("Key");
		final TableViewerColumn tvc2 = new TableViewerColumn(paramTable,
				SWT.LEFT);
		tvc2.getColumn().setWidth(120);
		tvc2.getColumn().setText("Value");
		final TableViewerColumn tvc3 = new TableViewerColumn(paramTable,
				SWT.LEFT);
		tvc3.getColumn().setText("Memo");
		paramTable.getTable().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent event) {
				Point size = ((Control) event.getSource()).getSize();
				int w = size.x - 4;
				ScrollBar vbar = ((Scrollable) event.getSource())
						.getVerticalBar();
				if (vbar != null && vbar.isVisible()) {
					w -= vbar.getSize().x;
				}
				int t = w / 9;
				if (t > 30) {
					tvc1.getColumn().setWidth(t * 2);
					tvc2.getColumn().setWidth(t * 4);
					tvc3.getColumn().setWidth(w - t * 6);
				} else {
					tvc1.getColumn().setWidth(60);
					tvc2.getColumn().setWidth(120);
					tvc3.getColumn().setWidth(90);
				}
			}
		});
		final IListChangeListener listChangeListener = new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {

					@Override
					public void handleAdd(int index, Object element) {
						validate((Parameter) element);
					}

					@Override
					public void handleRemove(int index, Object element) {
						clearError((Parameter) element);
					}
				});
				validateParameters((TestStep) stepTable.getElementAt(stepTable
						.getTable().getSelectionIndex()));
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
		paramTable.setContentProvider(contentProvider);
		final String[] propertyNames = new String[] { "key", "value", "memo" };
		final IObservableMap[] observableMaps = PojoObservables.observeMaps(
				contentProvider.getKnownElements(), Parameter.class,
				propertyNames);
		paramTable.setLabelProvider(new ObservableMapLabelProvider(
				observableMaps));
		paramTable.setCellEditors(new CellEditor[] {
				new TextCellEditor(paramTable.getTable(), SWT.LEFT),
				new TextCellEditor(paramTable.getTable(), SWT.LEFT),
				new TextCellEditor(paramTable.getTable(), SWT.LEFT) });
		paramTable.setColumnProperties(propertyNames);
		paramTable.setCellModifier(new ICellModifier() {

			@Override
			public void modify(Object element, String property, Object value) {
				Parameter param = (Parameter) ((Item) element).getData();
				Object oldValue = observableMaps[getIndex(property)].get(param);
				if (!Strings.equals(oldValue, value)) {
					observableMaps[getIndex(property)].put(param, value);
					paramTable.update(param, new String[] { property });
					validate(param);
					validateParameters((TestStep) stepTable
							.getElementAt(stepTable.getTable()
									.getSelectionIndex()));
					onFormChange();
				}
			}

			@Override
			public Object getValue(Object element, String property) {
				Object value = observableMaps[getIndex(property)].get(element);
				return Objects.toString(value);
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
		buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.FILL_VERTICAL));
		buttons.setLayout(UIUtils.createButtonsGridLayout());
		newParamButton = toolkit.createButton(buttons, "New", SWT.PUSH);
		newParamButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		newParamButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Parameter param = new Parameter();
				((WritableList) paramTable.getInput()).add(param);
				paramTable.setSelection(new StructuredSelection(param));
				paramTable.editElement(param, 0);
			}
		});
		removeParamButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		removeParamButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		removeParamButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int sel = paramTable.getTable().getSelectionIndex();
				((WritableList) paramTable.getInput()).remove(sel);
			}
		});
		moveParamUpButton = toolkit.createButton(buttons, "Up", SWT.PUSH);
		moveParamUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		moveParamUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = paramTable.getSelection();
				int sel = paramTable.getTable().getSelectionIndex();
				((WritableList) paramTable.getInput()).add(sel - 1,
						((WritableList) paramTable.getInput()).remove(sel));
				paramTable.setSelection(selection, true);
			}
		});
		moveParamDownButton = toolkit.createButton(buttons, "Down", SWT.PUSH);
		moveParamDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		moveParamDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = paramTable.getSelection();
				int sel = paramTable.getTable().getSelectionIndex();
				((WritableList) paramTable.getInput()).add(sel,
						((WritableList) paramTable.getInput()).remove(sel + 1));
				paramTable.setSelection(selection, true);
			}
		});
		toolkit.paintBordersFor(client);
		paramTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setParamTableButtonStates();
			}
		});
	}

	private void selectTestMethod() {
		if (!ensureJavaProject()) {
			return;
		}
		TestMethodSelectionDialog dialog = new TestMethodSelectionDialog(
				getEditorSite().getShell(), javaProject);
		dialog.setInitialPattern(invokeTargetText.getText());
		if (dialog.open() == Window.OK) {
			IAnnotation annotation = (IAnnotation) dialog.getResult()[0];
			String testMethod = AutoTestProjects
					.getTestMethodQualifiedName(annotation);
			invokeTargetText.setText(testMethod);
		}
	}

	private void selectTestData() {
		if (!ensureJavaProject()) {
			return;
		}
		TestDataSelectionDialog dialog = new TestDataSelectionDialog(
				getEditorSite().getShell(), javaProject);
		dialog.setInitialPattern(testDataText.getText());
		if (dialog.open() == Window.OK) {
			testDataText.setText(Objects.toString(dialog.getResult()[0]));
		}
	}

	private void selectTestScript() {
		if (!ensureJavaProject()) {
			return;
		}
		TestScriptSelectionDialog dialog = new TestScriptSelectionDialog(
				getEditorSite().getShell(), javaProject);
		dialog.setInitialPattern(invokeTargetText.getText());
		if (dialog.open() == Window.OK) {
			invokeTargetText.setText(Objects.toString(dialog.getResult()[0]));
		}
	}

	private boolean ensureJavaProject() {
		if (javaProject == null) {
			UIUtils.showError(getEditorSite().getShell(), "Error",
					"It's not a Java project.");
			return false;
		}
		return true;
	}

	private void setParamTableButtonStates() {
		int sel = paramTable.getTable().getSelectionIndex();
		removeParamButton.setEnabled(sel >= 0);
		moveParamUpButton.setEnabled(sel > 0);
		moveParamDownButton.setEnabled(sel >= 0
				&& sel < paramTable.getTable().getItemCount() - 1);
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
			invokeTargetText.setEditable(false);
			testDataText.setEditable(false);
			setStepTableButtonStates();
			if (detailSection.isVisible()) {
				setParamTableButtonStates();
			}
		}
	}

	@Override
	protected void bindControls(DataBindingContext dataBindingContext,
			TestScript model) {
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

	private final PropertyChangeListener dependenceChangeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			validateDependence();
		}
	};

	private void bindDetails(TestStep testStep) {
		clearError(testStep, false);
		dataBindingContext = new DataBindingContext();
		IManagedForm managedForm = getManagedForm();
		UIUtils.bindSelection(dataBindingContext, managedForm, invokeTypeCombo,
				testStep, "invokeType");
		UIUtils.bindText(dataBindingContext, managedForm, invokeTargetText,
				testStep, "invokeTarget");
		UIUtils.bindText(dataBindingContext, managedForm, testDataText,
				testStep, "testDataFile");
		UIUtils.bindText(dataBindingContext, managedForm, loopTimesText,
				testStep, "loopTimes", Converters.INT_PARSER,
				Converters.DEFAULT);
		UIUtils.bindText(dataBindingContext, managedForm, dependenceText,
				testStep, "dependentSteps", new StringToListConverter(
						Integer.class, true, true, true),
				new ListToStringConverter(true));
		testStep.addPropertyChangeListener(dependenceChangeListener);
		paramTable.setInput(testStep.getParameters());
	}

	private void unbindDetails(TestStep testStep) {
		if (dataBindingContext != null) {
			paramTable.setInput(WritableList.withElementType(Parameter.class));
			if (testStep != null) {
				testStep.removePropertyChangeListener(dependenceChangeListener);
			}
			UIUtils.unbind(dataBindingContext);
			dataBindingContext = null;
		}
		IMessageManager messageManager = getManagedForm().getMessageManager();
		messageManager.removeMessages(invokeTypeCombo.getCombo());
		messageManager.removeMessages(invokeTargetText);
		messageManager.removeMessages(testDataText);
		messageManager.removeMessages(loopTimesText);
		messageManager.removeMessages(dependenceText);
		if (!isDocumentError() && detailSection.getData() != null) {
			validate((TestStep) detailSection.getData(), false);
		}
	}

	@Override
	protected void postLoadModel(TestScript model) {
		super.postLoadModel(model);
		validateDependence();
		if (stepTable.getSelection().isEmpty()
				&& !model.getTestSteps().isEmpty()) {
			stepTable.setSelection(new StructuredSelection(model.getTestSteps()
					.get(0)), true);
		}
	}
}
