package cn.shaviation.autotest.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import cn.shaviation.autotest.builders.AutoTestProjectBuilder;
import cn.shaviation.autotest.util.ProjectUtils;

public class AutoTestProjectNature implements IProjectNature {

	public static final String ID = "cn.shaviation.autotest.nature";

	private IProject project;

	@Override
	public void configure() throws CoreException {
		ProjectUtils.addBuilders(project,
				new String[] { AutoTestProjectBuilder.ID }, null);
	}

	@Override
	public void deconfigure() throws CoreException {
		ProjectUtils.removeBuilders(project,
				new String[] { AutoTestProjectBuilder.ID }, null);
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
