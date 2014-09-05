package cn.shaviation.autotest.ui.internal.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
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

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		editor.setInput(input);
		if (editor.getEditorPage() != null) {
			editor.getEditorPage().setInput(input);
		}
	}

	@Override
	protected void setPartName(String partName) {
		super.setPartName(partName);
		editor.setPartName(partName);
	}

	@Override
	protected void setDocumentProvider(IEditorInput input) {
		IDocumentProvider documentProvider = getDocumentProvider();
		super.setDocumentProvider(input);
		onDocumentProviderChange(documentProvider);
	}

	@Override
	protected void disposeDocumentProvider() {
		IDocumentProvider documentProvider = getDocumentProvider();
		super.disposeDocumentProvider();
		onDocumentProviderChange(documentProvider);
	}

	private void onDocumentProviderChange(
			final IDocumentProvider documentProvider) {
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

	public IDocument getDocument() {
		return getDocumentProvider().getDocument(getEditorInput());
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
		String source = convertModelToSource(editor.getEditorPage().getModel());
		getDocument().set(source);
	}
}
