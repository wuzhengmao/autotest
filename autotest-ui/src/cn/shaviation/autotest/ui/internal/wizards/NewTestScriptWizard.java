package cn.shaviation.autotest.ui.internal.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.model.TestScript;
import cn.shaviation.autotest.core.model.TestScriptHelper;
import cn.shaviation.autotest.core.util.Logs;
import cn.shaviation.autotest.ui.AutoTestUI;

public class NewTestScriptWizard extends Wizard implements INewWizard {

	private IWorkbench workbench;
	private IStructuredSelection selection;
	private WizardNewFileCreationPage wizardPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle("New Test Script File");
	}

	@Override
	public void addPages() {
		wizardPage = new NewTestDataWizardPage(selection);
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		IFile newFile = wizardPage.createNewFile();
		IWorkbenchPage workbenchPage = workbench.getActiveWorkbenchWindow()
				.getActivePage();
		if (newFile != null && workbenchPage != null) {
			try {
				IDE.openEditor(workbenchPage, newFile,
						AutoTestUI.TEST_SCRIPT_EDITOR_ID, true);
			} catch (PartInitException e) {
				Logs.e(e);
			}
		}
		return true;
	}

	public static class NewTestDataWizardPage extends WizardNewFileCreationPage {

		public NewTestDataWizardPage(IStructuredSelection selection) {
			super("NewTestScriptWizardPage", selection);
			setTitle("Test Script");
			setDescription("Create a new Test Script file.");
			setFileExtension(AutoTestCore.TEST_SCRIPT_FILE_EXTENSION);
		}

		@Override
		protected InputStream getInitialContents() {
			TestScript testScript = TestScriptHelper.createDefault();
			try {
				String json = TestScriptHelper.serialize(testScript);
				return new ByteArrayInputStream(json.getBytes(ResourcesPlugin
						.getWorkspace().getRoot().getDefaultCharset()));
			} catch (Exception e) {
				return super.getInitialContents();
			}
		}
	}
}
