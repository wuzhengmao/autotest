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
				if (bin.exists()) {
					classpathEntries.add(createClasspathEntry(bin, new File(
							path, "src"), true));
				} else {
					classpathEntries
							.add(createClasspathEntry(path, null, true));
				}
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
									null, false));
						} catch (IOException e) {
							Logs.e(e);
						}
					}
				}
			}
		} else {
			try {
				classpathEntries.add(createClasspathEntry(path, null, true));
			} catch (IOException e) {
				Logs.e(e);
			}
		}
		return classpathEntries.toArray(new IClasspathEntry[classpathEntries
				.size()]);
	}

	private static String withVariable(String lib) throws IOException {
		for (String name : JavaCore.getClasspathVariableNames()) {
			IPath vp = JavaCore.getClasspathVariable(name);
			if (vp != null && !vp.isEmpty()) {
				String var = vp.toFile().getCanonicalPath();
				if (lib.startsWith(var)) {
					lib = lib.substring(var.length());
					if (!lib.startsWith("/") && !lib.startsWith("\\")) {
						lib = "/" + lib;
					}
					return name + lib;
				}
			}
		}
		return null;
	}

	private static IClasspathEntry createClasspathEntry(File path,
			File srcPath, boolean primary) throws IOException {
		String p = path.getCanonicalPath();
		String sp = srcPath != null ? srcPath.getCanonicalPath() : null;
		String vp = withVariable(p);
		String vsp = sp != null ? withVariable(sp) : null;
		if (vp != null || vsp != null) {
			return JavaCore.newVariableEntry(new Path(vp != null ? vp : p),
					sp != null ? new Path(vsp != null ? vsp : sp) : null,
					sp != null ? new Path("") : null,
					primary ? createAccessRules() : null, null, false);
		}
		return JavaCore.newLibraryEntry(new Path(p), sp != null ? new Path(sp)
				: null, sp != null ? new Path("") : null,
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
