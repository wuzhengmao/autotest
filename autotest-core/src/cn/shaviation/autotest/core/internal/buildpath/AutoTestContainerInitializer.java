package cn.shaviation.autotest.core.internal.buildpath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import cn.shavation.autotest.AutoTest;
import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.util.Logs;

public class AutoTestContainerInitializer extends ClasspathContainerInitializer {

	@Override
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		if (isValidContainerPath(containerPath)) {
			AutoTestContainer container = new AutoTestContainer(containerPath,
					createLibraryClasspathEntries());
			JavaCore.setClasspathContainer(containerPath,
					new IJavaProject[] { project },
					new IClasspathContainer[] { container }, null);
		}
	}

	@Override
	public Object getComparisonID(IPath containerPath, IJavaProject project) {
		return containerPath;
	}

	@Override
	public String getDescription(IPath containerPath, IJavaProject project) {
		if (isValidContainerPath(containerPath)) {
			return "Automatic Testing Library";
		}
		return "Unresolved container";
	}

	private static boolean isValidContainerPath(IPath path) {
		return path != null && path.segmentCount() == 1
				&& AutoTestCore.CONTAINER_ID.equals(path.segment(0));
	}

	private static IClasspathEntry[] createLibraryClasspathEntries() {
		List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
		File path;
		try {
			path = FileLocator.getBundleFile(AutoTest.Plugin.getDefault()
					.getBundle());
		} catch (IOException e) {
			Logs.e(e);
			return new IClasspathEntry[0];
		}
		if (path.isDirectory()) {
			try {
				File bin = new File(path, "bin");
				classpathEntries.add(createClasspathEntry(bin.exists() ? bin
						: path, true));
			} catch (IOException e) {
				Logs.e(e);
			}
			File libs = new File(path, "lib");
			if (libs.exists() && libs.isDirectory()) {
				for (File lib : libs.listFiles()) {
					String libName = lib.getName().toLowerCase();
					if (libName.endsWith(".jar")
							&& libName.startsWith("jackson-")) {
						try {
							classpathEntries.add(createClasspathEntry(lib,
									false));
						} catch (IOException e) {
							Logs.e(e);
						}
					}
				}
			}
		} else {
			try {
				classpathEntries.add(createClasspathEntry(path, true));
			} catch (IOException e) {
				Logs.e(e);
			}
		}
		return classpathEntries.toArray(new IClasspathEntry[classpathEntries
				.size()]);
	}

	private static IClasspathEntry createClasspathEntry(File path,
			boolean primary) throws IOException {
		String lib = path.getCanonicalPath();
		for (String name : JavaCore.getClasspathVariableNames()) {
			IPath vp = JavaCore.getClasspathVariable(name);
			if (vp != null && !vp.isEmpty()) {
				String var = vp.toFile().getCanonicalPath();
				if (lib.startsWith(var)) {
					lib = lib.substring(var.length());
					if (!lib.startsWith("/") && !lib.startsWith("\\")) {
						lib = "/" + lib;
					}
					return JavaCore.newVariableEntry(new Path(name + lib),
							null, null, primary ? createAccessRules() : null,
							null, false);
				}
			}
		}
		return JavaCore.newLibraryEntry(new Path(lib), null, null,
				primary ? createAccessRules() : null, null, false);
	}

	private static IAccessRule[] createAccessRules() {
		return new IAccessRule[] { JavaCore.newAccessRule(new Path(
				"**/internal/**/*"), IAccessRule.K_NON_ACCESSIBLE) };
	}

	public static class AutoTestContainer implements IClasspathContainer {

		private final IClasspathEntry[] entries;
		private final IPath path;

		public AutoTestContainer(IPath path, IClasspathEntry[] entries) {
			this.path = path;
			this.entries = entries;
		}

		@Override
		public IClasspathEntry[] getClasspathEntries() {
			return entries;
		}

		@Override
		public String getDescription() {
			return "Automatic Testing Library";
		}

		@Override
		public int getKind() {
			return IClasspathContainer.K_APPLICATION;
		}

		@Override
		public IPath getPath() {
			return path;
		}
	}
}
