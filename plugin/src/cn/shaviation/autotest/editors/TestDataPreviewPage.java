package cn.shaviation.autotest.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.util.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TestDataPreviewPage extends TestDataFormPage {

	private StyledText preview;

	public TestDataPreviewPage(TestDataEditor editor) {
		super(editor, "cn.shaviation.autotest.editors.TestDataPreviewPage",
				"Preview");
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.setText("Preview");
		Composite body = form.getBody();
		FillLayout layout = new FillLayout();
		body.setLayout(layout);
		preview = new StyledText(body, SWT.H_SCROLL | SWT.V_SCROLL);
		preview.setEditable(false);
		toolkit.paintBordersFor(body);
		super.createFormContent(managedForm);
	}

	@Override
	protected void fillForm(TestDataDef testDataDef) {
		try {
			preview.setText(getEditorInput().getObjectMapper()
					.writeValueAsString(testDataDef));
		} catch (JsonProcessingException e) {
			Utils.showError(this, "Cannot serialize test data definition!", e);
		}
	}
}
