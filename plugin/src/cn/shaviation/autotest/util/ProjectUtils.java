package cn.shaviation.autotest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProjectDescription;

public abstract class ProjectUtils {

	public static void addNatures(IProjectDescription description,
			String... natureIds) {
		List<String> list = new ArrayList<String>(Arrays.asList(description
				.getNatureIds()));
		for (String id : natureIds) {
			list.add(id);
		}
		description.setNatureIds(list.toArray(new String[list.size()]));
	}

	public static void addBuilders(IProjectDescription description,
			String... builderIds) {
		List<ICommand> buildSpec = new ArrayList<ICommand>(
				Arrays.asList(description.getBuildSpec()));
		for (String id : builderIds) {
			ICommand command = description.newCommand();
			command.setBuilderName(id);
			buildSpec.add(command);
		}
		description.setBuildSpec(buildSpec.toArray(new ICommand[buildSpec
				.size()]));
	}

	public static void removeBuilders(IProjectDescription description,
			String... builderIds) {
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
	}
}
