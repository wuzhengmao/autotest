package cn.shaviation.autotest.editors;

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension2;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;

import cn.shaviation.autotest.util.PartListenerAdapter;
import cn.shaviation.autotest.util.ShellListenerAdapter;
import cn.shaviation.autotest.util.UIUtils;

public class TestDataEditor extends FormEditor {

	private TestDataFormPage editorPage;
	private TestDataSourcePage sourcePage;
	private long lastConfirmSyncTime;
	private boolean ignoreCheck = false;

	private ShellListener shellListener = new ShellListenerAdapter() {
		@Override
		public void shellActivated(ShellEvent event) {
			if (TestDataEditor.this.equals(getEditorSite().getWorkbenchWindow()
					.getActivePage().getActivePart())) {
				onActive();
			}
		}
	};

	private IPartListener partListener = new PartListenerAdapter() {
		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part.equals(TestDataEditor.this)) {
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
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		TestDataEditorInput input = new TestDataEditorInput(
				(IFileEditorInput) editorInput);
		super.init(site, input);
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
	public TestDataEditorInput getEditorInput() {
		return (TestDataEditorInput) super.getEditorInput();
	}

	@Override
	protected void addPages() {
		try {
			sourcePage = new TestDataSourcePage(this);
			setPageText(addPage(sourcePage, getEditorInput()), "Source");
		} catch (PartInitException e) {
			UIUtils.showError(this, "Initialize source page failed!", e);
		}
		createEditorPage();
		setActivePage(1);
	}

	private void createEditorPage() {
		try {
			editorPage = new TestDataFormPage(this);
			addPage(editorPage);
		} catch (PartInitException e) {
			UIUtils.showError(this, "Initialize visual editor page failed!", e);
		}
	}

	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		notifyPageActive(newPageIndex);
	}

	private void onActive() {
		if (!ignoreCheck && checkDocumentStatus() == null) {
			if (getEditorInput().getDocumentProvider().isDeleted(
					getEditorInput())) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						confirmRemoveDocument();
					}
				});
			} else if (!isDocumentSynchronized()
					&& lastConfirmSyncTime < getEditorInput()
							.getDocumentProvider().getModificationStamp(
									getEditorInput())) {
				lastConfirmSyncTime = getEditorInput().getDocumentProvider()
						.getModificationStamp(getEditorInput());
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						confirmSynchronizeDocument();
					}
				});
			}
		}
	}

	private void confirmSynchronizeDocument() {
		ignoreCheck = true;
		if (MessageDialog
				.openQuestion(
						getEditorSite().getShell(),
						"File Changed",
						"The file '"
								+ getEditorInput().getFile().getFullPath()
								+ "' has been changed on the file system. Do you want to replace the editor contents with these changes?")) {
			try {
				((IDocumentProviderExtension) getEditorInput()
						.getDocumentProvider()).synchronize(getEditorInput());
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
				"File Not Accessible",
				null,
				"The file '"
						+ getEditorInput().getFile().getFullPath()
						+ "' has been deleted or is not accessible. Do you want to save your changes or close the editor without saving?",
				MessageDialog.QUESTION, new String[] { "Save", "Close" }, 0)
				.open() == 0) {
			performSave(((IDocumentProviderExtension2) getEditorInput()
					.getDocumentProvider()).getProgressMonitor());
		} else {
			close(false);
		}
		ignoreCheck = false;
	}

	public boolean isDocumentSynchronized() {
		IDocumentProvider provider = getEditorInput().getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension3) {
			return ((IDocumentProviderExtension3) provider)
					.isSynchronized(getEditorInput());
		}
		return true;
	}

	public IStatus checkDocumentStatus() {
		if (getEditorInput().getDocumentProvider() instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension = (IDocumentProviderExtension) getEditorInput()
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
							"The file '"
									+ getEditorInput().getFile().getFullPath()
									+ "' has been changed on the file system. Do you want to overwrite the changes made on the file system?")) {
				return;
			}
		}
		performSave(monitor);
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
			getEditorInput().getTestDataDef().setLastUpdateTime(new Date());
			sourcePage.reloadSource(true);
			getEditorInput().getDocumentProvider().saveDocument(monitor,
					getEditorInput(), getEditorInput().getDocument(), true);
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
		return getEditorInput().getDocumentProvider().canSaveDocument(
				getEditorInput());
	}

	public TestDataFormPage getEditorPage() {
		return editorPage;
	}

	public TestDataSourcePage getSourcePage() {
		return sourcePage;
	}
}
