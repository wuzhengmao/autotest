package cn.shaviation.autotest.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import cn.shaviation.autotest.models.TestDataHelper;
import cn.shaviation.autotest.util.UIUtils;

public class TestDataSourcePage extends TextEditor {

	private TestDataEditor editor;

	private long lastSyncTime;

	public TestDataSourcePage(TestDataEditor editor) {
		super();
		this.editor = editor;
	}

	@Override
	public TestDataEditorInput getEditorInput() {
		return (TestDataEditorInput) super.getEditorInput();
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		if (!(input instanceof TestDataEditorInput)) {
			getEditorInput().setFileEditorInput((IFileEditorInput) input);
			input = getEditorInput();
		}
		super.doSetInput(input);
	}

	@Override
	protected void setPartName(String partName) {
		super.setPartName(partName);
		editor.setPartName(partName);
	}

	@Override
	protected void setDocumentProvider(IEditorInput input) {
		super.setDocumentProvider(input);
		onDocumentProviderChange();
	}

	@Override
	protected void disposeDocumentProvider() {
		super.disposeDocumentProvider();
		onDocumentProviderChange();
	}

	private void onDocumentProviderChange() {
		final IDocumentProvider documentProvider = getEditorInput()
				.getDocumentProvider();
		getEditorInput().setDocumentProvider(getDocumentProvider());
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.getEditorPage().onDocumentProviderChange(
						documentProvider);
			}
		});
	}

	@Override
	public void close(boolean save) {
		super.close(save);
		editor.close(save);
	}

	public void onActive() {
		try {
			reloadSource(false);
		} catch (Exception e) {
			UIUtils.showError(this, "Generate source failed!", e);
		}
	}

	public void reloadSource(boolean force) throws Exception {
		if (force || lastSyncTime < editor.getEditorPage().getLastModifyTime()) {
			loadSource();
			lastSyncTime = editor.getEditorPage().getLastModifyTime();
		}
	}

	private void loadSource() throws Exception {
		String json = TestDataHelper.serialize(getEditorInput()
				.getTestDataDef());
		getEditorInput().getDocument().set(json);
	}
}
