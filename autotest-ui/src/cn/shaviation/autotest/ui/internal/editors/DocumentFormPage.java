package cn.shaviation.autotest.ui.internal.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessagePrefixProvider;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import cn.shaviation.autotest.ui.internal.util.DocumentListenerAdapter;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.util.Strings;

public abstract class DocumentFormPage<T> extends FormPage {

	protected DefaultModifyListener defaultModifyListener = new DefaultModifyListener();
	private DataBindingContext dataBindingContext;
	private boolean ignoreChange = false;
	private boolean ignoreReload = false;
	private boolean needReload = true;
	private boolean documentError = false;
	private long lastModifyTime;

	private IDocumentListener documentListener = new DocumentListenerAdapter() {
		@Override
		public void documentChanged(DocumentEvent event) {
			if (!ignoreReload) {
				needReload = true;
				if (isActive() && getEditor().isActive()) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							loadModel();
							needReload = false;
						}
					});
				}
			}
		}
	};

	public DocumentFormPage(DocumentFormEditor<T> editor, String id,
			String title) {
		super(editor, id, title);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentFormEditor<T> getEditor() {
		return (DocumentFormEditor<T>) super.getEditor();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentEditorInput<T> getEditorInput() {
		return (DocumentEditorInput<T>) super.getEditorInput();
	}

	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		if (getEditorInput().getModel() == null) {
			getEditorInput().setModel(createModel());
		}
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		IToolBarManager toolBarManager = form.getToolBarManager();
		toolBarManager.add(new Action("Refresh", UIUtils
				.getImageDescriptor("refresh.gif")) {
			public void run() {
				getEditor().getSourcePage()
						.getAction(ITextEditorActionConstants.REFRESH).run();
			}
		});
		form.updateToolBar();
		toolkit.decorateFormHeading(form.getForm());
		form.getMessageManager().setMessagePrefixProvider(
				new IMessagePrefixProvider() {
					@Override
					public String getPrefix(Control control) {
						return null;
					}
				});
	}

	protected void setError(String key, String error) {
		if (error != null) {
			getManagedForm().getMessageManager().addMessage(key, error, null,
					IMessageProvider.ERROR);
		} else {
			getManagedForm().getMessageManager().removeMessage(key);
		}
	}

	public void onDocumentProviderChange(IDocumentProvider documentProvider) {
		if (documentProvider != null
				&& documentProvider.getDocument(getEditorInput()) != null) {
			documentProvider.getDocument(getEditorInput())
					.removeDocumentListener(documentListener);
		}
		if (getEditorInput().getDocumentProvider() != null) {
			getEditorInput().getDocumentProvider()
					.getDocument(getEditorInput())
					.addDocumentListener(documentListener);
		}
	}

	public void onActive() {
		if (needReload) {
			loadModel();
			needReload = false;
		}
	}

	private void loadModel() {
		T model = createModel();
		IStatus status = getEditor().checkDocumentStatus();
		if (status != null) {
			documentError(status.getMessage(), IMessageProvider.WARNING);
			documentError = true;
		} else {
			try {
				String source = getEditorInput().getDocument().get();
				if (!Strings.isEmpty(source)) {
					model = convertSourceToModel(source);
				} else {
					initModel(model);
				}
				clearErrorMessage();
				documentError = false;
			} catch (Exception e) {
				documentError(e.getMessage(), IMessageProvider.ERROR);
				documentError = true;
			}
		}
		ignoreChange = true;
		mergeModel(model, getEditorInput().getModel());
		if (!documentError) {
			if (dataBindingContext == null) {
				dataBindingContext = new DataBindingContext();
				bindControls(dataBindingContext, getEditorInput().getModel());
			}
		}
		ignoreChange = false;
		lastModifyTime = 0;
		boolean readonly = documentError;
		if (!readonly) {
			try {
				readonly = getEditorInput().getStorage().isReadOnly();
			} catch (CoreException e) {
			}
		}
		enableControls(readonly);
		postLoadModel(getEditorInput().getModel());
	}

	private void documentError(String message, int severity) {
		if (dataBindingContext != null) {
			unbindControls(dataBindingContext);
			dataBindingContext = null;
		}
		setErrorMessage(message, severity);
	}

	protected abstract T createModel();

	protected void initModel(T model) {

	}

	protected abstract T convertSourceToModel(String source) throws Exception;

	protected abstract void mergeModel(T source, T target);

	protected void enableControls(boolean readonly) {
		UIUtils.setReadonly((Composite) getPartControl(), readonly);
	}

	protected abstract void bindControls(DataBindingContext dataBindingContext,
			T model);

	protected void unbindControls(DataBindingContext dataBindingContext) {
		UIUtils.unbind(dataBindingContext);
		clearAllErrorMessages();
	}

	protected void postLoadModel(T model) {

	}

	public String getErrorMessage() {
		if (getManagedForm().getForm().getMessageType() == IMessageProvider.ERROR
				|| getManagedForm().getForm().getMessageType() == IMessageProvider.WARNING) {
			String error = getManagedForm().getMessageManager().createSummary(
					getManagedForm().getForm().getForm().getChildrenMessages());
			if (Strings.isEmpty(error)) {
				error = getManagedForm().getForm().getMessage();
			}
			return error;
		}
		return null;
	}

	private void clearAllErrorMessages() {
		getManagedForm().getForm().getMessageManager().removeAllMessages();
	}

	private void clearErrorMessage() {
		UIUtils.setMessage(getManagedForm().getForm(), null,
				IMessageProvider.NONE);
	}

	private void setErrorMessage(final String msg, int severity) {
		if ((getPartControl() != null) && (!getPartControl().isDisposed())) {
			if (!getManagedForm().getForm().isDisposed()) {
				UIUtils.setMessage(getManagedForm().getForm(), msg, severity);
			}
		}
	}

	protected void onFormChange() {
		if (!ignoreChange) {
			lastModifyTime = System.nanoTime();
			if (!getEditor().isDirty()) {
				ignoreReload = true;
				try {
					((IDocumentExtension4) getEditorInput().getDocument())
							.replace(0, 0, "", System.currentTimeMillis());
				} catch (Exception e) {
					getEditorInput().getDocument().set(
							getEditorInput().getDocument().get());
				}
				ignoreReload = false;
				getEditor().editorDirtyStateChanged();
			}
		}
	}

	protected void setIgnoreChange(boolean ignoreChange) {
		this.ignoreChange = ignoreChange;
	}

	public boolean isDocumentError() {
		return documentError;
	}

	public long getLastModifyTime() {
		return lastModifyTime;
	}

	private class DefaultModifyListener implements ModifyListener,
			ISelectionChangedListener, SelectionListener,
			PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			onFormChange();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {

		}

		@Override
		public void widgetSelected(SelectionEvent event) {
			onFormChange();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			onFormChange();
		}

		@Override
		public void modifyText(ModifyEvent event) {
			onFormChange();
		}
	}
}
