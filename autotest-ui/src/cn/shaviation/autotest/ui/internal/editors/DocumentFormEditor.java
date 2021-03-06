package cn.shaviation.autotest.ui.internal.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension2;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;

import cn.shaviation.autotest.ui.internal.util.PartListenerAdapter;
import cn.shaviation.autotest.ui.internal.util.ShellListenerAdapter;
import cn.shaviation.autotest.ui.internal.util.UIUtils;

public abstract class DocumentFormEditor<T> extends FormEditor {

	private DocumentFormPage<T> editorPage;
	private DocumentSourcePage<T> sourcePage;
	private long lastConfirmSyncTime;
	private boolean ignoreCheck = false;

	private ShellListener shellListener = new ShellListenerAdapter() {
		@Override
		public void shellActivated(ShellEvent event) {
			if (DocumentFormEditor.this.equals(getEditorSite()
					.getWorkbenchWindow().getActivePage().getActivePart())) {
				onActive();
			}
		}
	};

	private IPartListener partListener = new PartListenerAdapter() {
		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part.equals(DocumentFormEditor.this)) {
				onActive();
			}
		}
	};

	@Override
	public void dispose() {
		getEditorSite().getWorkbenchWindow().getPartService()
				.removePartListener(partListener);
		getEditorSite().getShell().removeShellListener(shellListener);
		super.dispose();
	}

	@Override
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		if (!(editorInput instanceof IStorageEditorInput)) {
			throw new PartInitException(
					"Invalid Input: Must be IStorageEditorInput");
		}
		super.init(site, editorInput);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				getEditorSite().getWorkbenchWindow().getPartService()
						.addPartListener(partListener);
				getEditorSite().getShell().addShellListener(shellListener);
			}
		});
	}

	@Override
	public void setPartName(String partName) {
		super.setPartName(partName);
	}

	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		super.firePropertyChange(PROP_TITLE);
	}

	@Override
	public IStorageEditorInput getEditorInput() {
		return (IStorageEditorInput) super.getEditorInput();
	}

	@Override
	protected void addPages() {
		try {
			sourcePage = createSourcePage();
			setPageText(addPage(sourcePage, getEditorInput()), "Source");
		} catch (PartInitException e) {
			UIUtils.showError(this, "Initialize source page failed!", e);
		}
		try {
			editorPage = createEditorPage();
			addPage(editorPage);
		} catch (PartInitException e) {
			UIUtils.showError(this, "Initialize visual editor page failed!", e);
		}
		setActivePage(1);
	}

	protected abstract DocumentSourcePage<T> createSourcePage();

	protected abstract DocumentFormPage<T> createEditorPage();

	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		notifyPageActive(newPageIndex);
	}

	private void onActive() {
		if (!ignoreCheck && checkDocumentStatus() == null) {
			if (sourcePage.getDocumentProvider().isDeleted(getEditorInput())) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						confirmRemoveDocument();
					}
				});
				return;
			} else if (!isDocumentSynchronized()
					&& lastConfirmSyncTime < sourcePage.getDocumentProvider()
							.getModificationStamp(getEditorInput())) {
				lastConfirmSyncTime = sourcePage.getDocumentProvider()
						.getModificationStamp(getEditorInput());
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						confirmSynchronizeDocument();
					}
				});
				return;
			}
		}
		notifyPageActive(getActivePage());
	}

	public boolean isReadonly() {
		try {
			return getEditorInput().getStorage().isReadOnly();
		} catch (CoreException e) {
			return true;
		}
	}

	public IPath getResourcePath() {
		try {
			return getEditorInput().getStorage().getFullPath();
		} catch (CoreException e) {
		}
		return null;
	}

	private void confirmSynchronizeDocument() {
		ignoreCheck = true;
		if (MessageDialog
				.openQuestion(
						getEditorSite().getShell(),
						"Resource Changed",
						"The resource '"
								+ getResourcePath()
								+ "' has been changed on the file system. Do you want to replace the editor contents with these changes?")) {
			try {
				((IDocumentProviderExtension) sourcePage.getDocumentProvider())
						.synchronize(getEditorInput());
			} catch (CoreException e) {
				UIUtils.showError(this, "Synchronize file failed!", e);
			}
		}
		ignoreCheck = false;
	}

	private void confirmRemoveDocument() {
		ignoreCheck = true;
		if (new MessageDialog(
				getEditorSite().getShell(),
				"Resource Not Accessible",
				null,
				"The resource '"
						+ getResourcePath()
						+ "' has been deleted or is not accessible. Do you want to save your changes or close the editor without saving?",
				MessageDialog.QUESTION, new String[] { "Save", "Close" }, 0)
				.open() == 0) {
			performSave(((IDocumentProviderExtension2) sourcePage
					.getDocumentProvider()).getProgressMonitor());
		} else {
			close(false);
		}
		ignoreCheck = false;
	}

	public boolean isDocumentSynchronized() {
		IDocumentProvider provider = sourcePage.getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension3) {
			return ((IDocumentProviderExtension3) provider)
					.isSynchronized(getEditorInput());
		}
		return true;
	}

	public IStatus checkDocumentStatus() {
		if (sourcePage.getDocumentProvider() instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension = (IDocumentProviderExtension) sourcePage
					.getDocumentProvider();
			IStatus status = extension.getStatus(getEditorInput());
			if (status != null && status.getSeverity() == IStatus.ERROR) {
				return status;
			}
		}
		return null;
	}

	private void notifyPageActive(int page) {
		if (page == 0) {
			sourcePage.onActive();
		} else {
			editorPage.onActive();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (!isDocumentSynchronized()) {
			if (!MessageDialog
					.openQuestion(
							getEditorSite().getShell(),
							"Update conflict",
							"The resource '"
									+ getResourcePath()
									+ "' has been changed on the file system. Do you want to overwrite the changes made on the file system?")) {
				return;
			}
		}
		performSave(monitor);
	}

	protected void beforeSave(T model) {

	}

	private void performSave(IProgressMonitor monitor) {
		if (getActivePage() == 0) {
			editorPage.onActive();
		}
		if (editorPage.isDocumentError()) {
			MessageDialog.openError(getEditorSite().getShell(), "Error",
					editorPage.getErrorMessage());
			return;
		}
		try {
			beforeSave(editorPage.getModel());
			sourcePage.reloadSource(true);
			sourcePage.getDocumentProvider().saveDocument(monitor,
					getEditorInput(), sourcePage.getDocument(), true);
		} catch (Exception e) {
			UIUtils.showError(this, "Save failed!", e);
			return;
		}
		if (getActivePage() == 0) {
			setActivePage(1);
		}
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public boolean isActive() {
		return this.equals(getEditorSite().getWorkbenchWindow().getActivePage()
				.getActiveEditor());
	}

	@Override
	public boolean isDirty() {
		return sourcePage.getDocumentProvider().canSaveDocument(
				getEditorInput());
	}

	public DocumentFormPage<T> getEditorPage() {
		return editorPage;
	}

	public DocumentSourcePage<T> getSourcePage() {
		return sourcePage;
	}
}
