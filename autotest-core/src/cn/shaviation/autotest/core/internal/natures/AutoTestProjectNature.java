package cn.shaviation.autotest.core.internal.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.util.Projects;

public class AutoTestProjectNature implements IProjectNature {

	private IProject project;

	@Override
	public void configure() throws CoreException {
		Projects.addBuilders(project, new String[] { AutoTestCore.BUILDER_ID },
				null);
	}

	@Override
	public void deconfigure() throws CoreException {
		Projects.removeBuilders(project,
				new String[] { AutoTestCore.BUILDER_ID }, null);
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
}
