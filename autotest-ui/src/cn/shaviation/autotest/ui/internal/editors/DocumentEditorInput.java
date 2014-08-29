package cn.shaviation.autotest.ui.internal.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class DocumentEditorInput<T> implements IFileEditorInput {

	private IFileEditorInput fileEditorInput;
	private IDocumentProvider documentProvider;
	private T model;

	public DocumentEditorInput(IFileEditorInput fileEditorInput) {
		this.fileEditorInput = fileEditorInput;
	}

	@Override
	public IStorage getStorage() throws CoreException {
		return fileEditorInput.getStorage();
	}

	@Override
	public boolean exists() {
		return fileEditorInput.exists();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return fileEditorInput.getImageDescriptor();
	}

	@Override
	public String getName() {
		return fileEditorInput.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return fileEditorInput.getPersistable();
	}

	@Override
	public String getToolTipText() {
		return fileEditorInput.getToolTipText();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class klass) {
		return fileEditorInput.getAdapter(klass);
	}

	@Override
	public IFile getFile() {
		return fileEditorInput.getFile();
	}

	@Override
	public int hashCode() {
		return fileEditorInput.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return fileEditorInput.equals(obj);
	}

	public IDocumentProvider getDocumentProvider() {
		return documentProvider;
	}

	public void setDocumentProvider(IDocumentProvider documentProvider) {
		this.documentProvider = documentProvider;
	}

	public IDocument getDocument() {
		return documentProvider.getDocument(this);
	}

	public T getModel() {
		return model;
	}

	public void setModel(T model) {
		this.model = model;
	}

	public void setFileEditorInput(IFileEditorInput fileEditorInput) {
		this.fileEditorInput = fileEditorInput;
	}
}
