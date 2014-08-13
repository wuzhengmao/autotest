package cn.shaviation.autotest.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.util.Utils;

public abstract class TestDataFormPage extends FormPage {

	private long lastLoadTime;

	public TestDataFormPage(TestDataEditor editor, String id, String title) {
		super(editor, id, title);
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
		ScrolledForm form = managedForm.getForm();
		IToolBarManager toolBarManager = form.getToolBarManager();
		toolBarManager
				.add(new Action("Refresh", Utils.getImage("refresh.gif")) {
					public void run() {
						getEditor().reload();
					}
				});
		form.updateToolBar();
		managedForm.getToolkit().decorateFormHeading(form.getForm());
		checkModel();
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		if (active) {
			checkModel();
		}
	}

	protected abstract void fillForm(TestDataDef testDataDef);

	public void checkModel() {
		if (lastLoadTime != getEditorInput().getLastUpdateTime()) {
			fillForm(getEditorInput().getTestDataDef());
			lastLoadTime = getEditorInput().getLastUpdateTime();
		}
	}

	public void setErrorMessage(final String msg, final int severity) {
		if ((getPartControl() != null) && (!getPartControl().isDisposed()))
			getPartControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!TestDataFormPage.this.getManagedForm().getForm()
							.isDisposed())
						Utils.setMessage(TestDataFormPage.this.getManagedForm()
								.getForm(), msg, severity);
				}
			});
	}

}