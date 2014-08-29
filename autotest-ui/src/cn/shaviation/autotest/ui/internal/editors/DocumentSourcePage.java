package cn.shaviation.autotest.ui.internal.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import cn.shaviation.autotest.ui.internal.util.UIUtils;

public abstract class DocumentSourcePage<T> extends TextEditor {

	private DocumentFormEditor<T> editor;

	private long lastSyncTime;

	public DocumentSourcePage(DocumentFormEditor<T> editor) {
		super();
		this.editor = editor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentEditorInput<T> getEditorInput() {
		return (DocumentEditorInput<T>) super.getEditorInput();
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		if (!(input instanceof DocumentEditorInput)) {
			getEditorInput().setFileEditorInput((IFileEditorInput) input);
			input = getEditorInput();
		}
		super.doSetInput(input);
		editor.setInput(input);
		editor.getEditorPage().setInput(input);
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

	protected abstract String convertModelToSource(T model) throws Exception;

	private void loadSource() throws Exception {
		String source = convertModelToSource(getEditorInput().getModel());
		getEditorInput().getDocument().set(source);
	}
}
