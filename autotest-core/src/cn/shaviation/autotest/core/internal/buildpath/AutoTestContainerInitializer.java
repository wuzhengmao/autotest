package cn.shaviation.autotest.core.internal.buildpath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import cn.shaviation.autotest.AutoTest;
import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.util.Logs;
import cn.shaviation.autotest.util.Strings;
import cn.shaviation.autotest.webdriver.WebDriverRuntime;

public class AutoTestContainerInitializer extends ClasspathContainerInitializer {

	private static final String INTERNAL_PATTERN = "**/internal/**/*";

	@Override
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		if (isValidContainerPath(containerPath)) {
			AutoTestContainer container = new AutoTestContainer(containerPath,
					createLibraryClasspathEntries(containerPath));
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
			return getLibraryName(containerPath);
		}
		return "Unresolved container";
	}

	private static boolean isValidContainerPath(IPath path) {
		return path != null && path.segmentCount() >= 1
				&& AutoTestCore.CONTAINER_ID.equals(path.segment(0));
	}

	private static String getLibraryName(IPath path) {
		String name = "Automatic Testing Library";
		if (path.segmentCount() > 1) {
			String ext = AutoTestCore.getRuntimeExtensions().get(
					path.segment(1));
			if (!Strings.isEmpty(ext)) {
				name += " with " + ext;
			} else {
				name += " miss " + ext;
			}
		}
		return name;
	}

	private static IClasspathEntry[] createLibraryClasspathEntries(
			IPath containerPath) {
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
					classpathEntries.add(JavaUtils.createClasspathEntry(bin,
							new File(path, "src"),
							JavaUtils.createForbiddenRules(INTERNAL_PATTERN)));
				} else {
					classpathEntries.add(JavaUtils.createClasspathEntry(path,
							null,
							JavaUtils.createForbiddenRules(INTERNAL_PATTERN)));
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
							classpathEntries.add(JavaUtils
									.createClasspathEntry(lib, null, null));
						} catch (IOException e) {
							Logs.e(e);
						}
					}
				}
			}
		} else {
			try {
				classpathEntries.add(JavaUtils.createClasspathEntry(path, null,
						JavaUtils.createForbiddenRules(INTERNAL_PATTERN)));
			} catch (Exception e) {
				Logs.e(e);
			}
		}
		if (containerPath.segmentCount() > 1) {
			String ext = containerPath.segment(1);
			if (WebDriverRuntime.EXTENSION_ID.equals(ext)) {
				try {
					classpathEntries.addAll(createWebDriverClasspathEntries());
				} catch (Exception e) {
					Logs.e(e);
				}
			}
		}
		return classpathEntries.toArray(new IClasspathEntry[classpathEntries
				.size()]);
	}

	private static List<IClasspathEntry> createWebDriverClasspathEntries() {
		List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
		File path;
		try {
			path = FileLocator.getBundleFile(WebDriverRuntime.getDefault()
					.getBundle());
		} catch (IOException e) {
			Logs.e(e);
			return Collections.emptyList();
		}
		if (path.isDirectory()) {
			try {
				File bin = new File(path, "bin");
				if (bin.exists()) {
					classpathEntries.add(JavaUtils.createClasspathEntry(bin,
							new File(path, "src"),
							JavaUtils.createForbiddenRules(INTERNAL_PATTERN)));
				} else {
					classpathEntries.add(JavaUtils.createClasspathEntry(path,
							null,
							JavaUtils.createForbiddenRules(INTERNAL_PATTERN)));
				}
			} catch (IOException e) {
				Logs.e(e);
			}
			File libs = new File(path, "lib");
			if (libs.exists() && libs.isDirectory()) {
				for (File lib : libs.listFiles()) {
					String libName = lib.getName().toLowerCase();
					if (libName.endsWith(".jar")
							&& !libName.endsWith("-srcs.jar")) {
						String srcName = lib.getName().replace(".jar",
								"-srcs.jar");
						File srcLib = new File(lib.getParentFile(), srcName);
						if (!srcLib.exists()) {
							srcLib = null;
						}
						try {
							classpathEntries.add(JavaUtils
									.createClasspathEntry(lib, srcLib, null));
						} catch (IOException e) {
							Logs.e(e);
						}
					}
				}
			}
		} else {
			try {
				classpathEntries.add(JavaUtils.createClasspathEntry(path, null,
						JavaUtils.createForbiddenRules(INTERNAL_PATTERN)));
			} catch (Exception e) {
				Logs.e(e);
			}
		}
		return classpathEntries;
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
			return getLibraryName(path);
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
