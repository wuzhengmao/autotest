package cn.shaviation.autotest.ui.internal.buildpath;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class AutoTestContainerWizardPage extends NewElementWizardPage implements
		IClasspathContainerPage, IClasspathContainerPageExtension {

	private IClasspathEntry containerEntry = JavaCore
			.newContainerEntry(new Path(AutoTestCore.CONTAINER_ID));

	public AutoTestContainerWizardPage() {
		super("AutoTestContainerPage");
		setTitle("Automatic Testing Library");
		setDescription("Set the library of automatic testing environment.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		setControl(composite);
	}

	@Override
	public void initialize(IJavaProject javaProject,
			IClasspathEntry[] currentEntries) {

	}

	private static IJavaProject getPlaceholderProject() {
		String name = "####internal";
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (;;) {
			IProject project = root.getProject(name);
			if (!project.exists()) {
				return JavaCore.create(project);
			}
			name = name + '1';
		}
	}

	@Override
	public boolean finish() {
		try {
			IJavaProject[] javaProjects = { getPlaceholderProject() };
			IClasspathContainer[] containers = new IClasspathContainer[1];
			JavaCore.setClasspathContainer(containerEntry.getPath(),
					javaProjects, containers, null);
		} catch (JavaModelException e) {
			UIUtils.showError(getShell(), "Automatic Testing Library",
					"Problem while configuring the AutoTest container.", e);
			return false;
		}
		return true;
	}

	@Override
	public IClasspathEntry getSelection() {
		return containerEntry;
	}

	@Override
	public void setSelection(IClasspathEntry containerEntry) {

	}
}
