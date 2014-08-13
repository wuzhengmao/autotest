package cn.shaviation.autotest.editors;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.EditorPart;

import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.util.PartListenerAdapter;
import cn.shaviation.autotest.util.Utils;

public class TestDataEditor extends FormEditor implements
		IResourceChangeListener {

	private TestDataEditorPage editorPage;
	private TestDataPreviewPage previewPage;
	private TextEditor sourcePage;
	private boolean dirty = false;

	public TestDataEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	@Override
	protected void addPages() {
		try {
			editorPage = new TestDataEditorPage(this);
			addPage(editorPage);
		} catch (PartInitException e) {
			Utils.showError(this, "Create visual editor page failed!", e);
		}
		try {
			previewPage = new TestDataPreviewPage(this);
			addPage(previewPage);
		} catch (PartInitException e) {
			Utils.showError(this, "Create preview page failed!", e);
		}
		try {
			sourcePage = new TextEditor();
			setPageText(addPage(sourcePage, getEditorInput()), "Source");
			final IDocument document = sourcePage.getDocumentProvider().getDocument(getEditorInput());
			document.addDocumentListener(new IDocumentListener() {
				
				@Override
				public void documentChanged(DocumentEvent event) {
					setDirty(true);
					System.out.println(document.get());
				}
				
				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
					
				}
			});
		} catch (PartInitException e) {
			Utils.showError(this, "Create source page failed!", e);
		}
		loadTestData();
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
		setPartName(input.getFile().getName());
		getEditorSite().getWorkbenchWindow().getPartService()
				.addPartListener(new PartListenerAdapter() {
					@Override
					public void partBroughtToTop(IWorkbenchPart part) {
						if (part.equals(TestDataEditor.this)) {
							((TestDataFormPage) getActivePageInstance())
									.checkModel();
						}
					}
				});
	}

	@Override
	public TestDataEditorInput getEditorInput() {
		return (TestDataEditorInput) super.getEditorInput();
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE
				|| event.getType() == IResourceChangeEvent.PRE_DELETE) {
			if (getEditorInput().getFile().getProject()
					.equals(event.getResource())) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						close(false);
					}
				});
			}
		} else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			final IResourceDelta delta = event.getDelta().findMember(
					getEditorInput().getFile().getFullPath());
			if (delta != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (delta.getKind() == IResourceDelta.REMOVED) {
							if (delta.getFlags() == IResourceDelta.MOVED_TO) {
								IFile newFile = ResourcesPlugin.getWorkspace()
										.getRoot()
										.getFile(delta.getMovedToPath());
								getEditorInput().setFile(newFile);
							} else {
								close(false);
							}
						} else if (delta.getKind() == IResourceDelta.CHANGED) {
							reload();
						}
					}
				});
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			String json = getEditorInput().getObjectMapper()
					.writeValueAsString(getEditorInput().getTestDataDef());
			IFile file = getEditorInput().getFile();
			file.setContents(
					new ByteArrayInputStream(json.getBytes(getFileCharset())),
					false, true, monitor);
			setDirty(false);
		} catch (Exception e) {
			Utils.showError(this, "Save failed!", e);
			monitor.setCanceled(true);
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

	public void reload() {
		loadTestData();
		if (isActive()) {
			((TestDataFormPage) getActivePageInstance()).checkModel();
		}
	}

	private void loadTestData() {
		TestDataDef testDataDef = new TestDataDef();
		IFile file = getEditorInput().getFile();
		try {
			testDataDef = getEditorInput().getObjectMapper().readValue(
					new InputStreamReader(file.getContents(false),
							getFileCharset()), TestDataDef.class);
		} catch (Exception e) {
			Utils.showError(this, "Load file failed!", e);
		}
		getEditorInput().setTestDataDef(testDataDef);
	}

	private String getFileCharset() {
		try {
			return getEditorInput().getFile().getCharset();
		} catch (CoreException e) {
		}
		try {
			return getEditorInput().getFile().getProject().getDefaultCharset();
		} catch (CoreException e) {
		}
		try {
			return getEditorInput().getFile().getWorkspace().getRoot()
					.getDefaultCharset();
		} catch (CoreException e) {
			return "UTF-8";
		}
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	protected void setDirty(boolean dirty) {
		this.dirty = dirty;
		firePropertyChange(EditorPart.PROP_DIRTY);
	}
}
