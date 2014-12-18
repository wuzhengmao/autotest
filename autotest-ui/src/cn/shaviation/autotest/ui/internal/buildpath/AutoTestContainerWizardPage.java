package cn.shaviation.autotest.ui.internal.buildpath;

import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.util.Strings;

public class AutoTestContainerWizardPage extends NewElementWizardPage implements
		IClasspathContainerPage, IClasspathContainerPageExtension {

	private IClasspathEntry containerEntry = JavaCore
			.newContainerEntry(new Path(AutoTestCore.CONTAINER_ID));
	private Combo libraryCombo;

	public AutoTestContainerWizardPage() {
		super("AutoTestContainerPage");
		setTitle("Automatic Testing Library");
		setDescription("Set the library of automatic testing environment.");
	}

	@Override
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(2, false));
		Label label = new Label(composite, SWT.NONE);
		label.setFont(composite.getFont());
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false,
				false, 1, 1));
		label.setText("Select Library:");
		libraryCombo = new Combo(composite, SWT.READ_ONLY);
		libraryCombo.setItems(AutoTestCore.getRuntimeExtensions().values()
				.toArray(new String[0]));
		libraryCombo.setFont(composite.getFont());
		GridData data = new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 1, 1);
		data.widthHint = converter.convertWidthInCharsToPixels(20);
		libraryCombo.setLayoutData(data);
		if (containerEntry != null) {
			libraryCombo.setText(getLibraryName(containerEntry.getPath()));
		} else {
			if (libraryCombo.getItemCount() > 1) {
				libraryCombo.select(1);
			} else {
				libraryCombo.select(0);
			}
		}
		libraryCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				doSelectionChanged();
			}
		});
		doSelectionChanged();
		setControl(composite);
	}

	private void doSelectionChanged() {
		String path = AutoTestCore.CONTAINER_ID;
		for (Entry<String, String> entry : AutoTestCore.getRuntimeExtensions()
				.entrySet()) {
			if (libraryCombo.getText().equals(entry.getValue())) {
				String id = entry.getKey();
				if (!Strings.isEmpty(id)) {
					path += "/" + id;
				}
				break;
			}
		}
		containerEntry = JavaCore.newContainerEntry(new Path(path));
	}

	private static String getLibraryName(IPath path) {
		if (path.segmentCount() > 1) {
			String name = AutoTestCore.getRuntimeExtensions().get(
					path.segment(1));
			if (!Strings.isEmpty(name)) {
				return name;
			}
		}
		return AutoTestCore.EXTENSION_NAME;
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
		this.containerEntry = containerEntry;
	}
}
