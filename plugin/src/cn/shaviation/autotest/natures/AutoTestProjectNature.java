package cn.shaviation.autotest.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import cn.shaviation.autotest.builders.AutoTestProjectBuilder;
import cn.shaviation.autotest.util.ProjectUtils;

public class AutoTestProjectNature implements IProjectNature {

	public static final String ID = "cn.shaviation.autotest.nature";

	private IProject project;

	@Override
	public void configure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ProjectUtils.addBuilders(description, AutoTestProjectBuilder.ID);
		project.setDescription(description, null);
	}

	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ProjectUtils.removeBuilders(description, AutoTestProjectBuilder.ID);
		project.setDescription(description, null);
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
