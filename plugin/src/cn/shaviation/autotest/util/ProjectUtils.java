package cn.shaviation.autotest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class ProjectUtils {

	public static void addNature(IProject project, String natureId,
			IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	public static void addBuilders(IProject project, String[] builderIds,
			IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		List<ICommand> buildSpec = new ArrayList<ICommand>(
				Arrays.asList(description.getBuildSpec()));
		for (String id : builderIds) {
			ICommand command = description.newCommand();
			command.setBuilderName(id);
			buildSpec.add(command);
		}
		description.setBuildSpec(buildSpec.toArray(new ICommand[buildSpec
				.size()]));
		project.setDescription(description, monitor);
	}

	public static void removeBuilders(IProject project, String[] builderIds,
			IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		List<ICommand> buildSpec = new ArrayList<ICommand>(
				Arrays.asList(description.getBuildSpec()));
		for (String id : builderIds) {
			for (int i = 0; i < buildSpec.size(); i++) {
				if (id.equals(buildSpec.get(i).getBuilderName())) {
					buildSpec.remove(i);
					break;
				}
			}
		}
		description.setBuildSpec(buildSpec.toArray(new ICommand[buildSpec
				.size()]));
		project.setDescription(description, monitor);
	}
}
