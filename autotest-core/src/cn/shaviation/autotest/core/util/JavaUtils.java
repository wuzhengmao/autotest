package cn.shaviation.autotest.core.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;

public abstract class JavaUtils {

	public static boolean isJavaProject(IProject project) {
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				return true;
			}
		} catch (CoreException e) {
		}
		return false;
	}

	public static IJavaProject getJavaProject(IProject project) {
		return isJavaProject(project) ? JavaCore.create(project) : null;
	}

	public static void addClasspathEntries(IJavaProject project,
			IClasspathEntry[] classpathEntries, IProgressMonitor monitor)
			throws JavaModelException {
		IClasspathEntry[] entries = project.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[entries.length
				+ classpathEntries.length];
		System.arraycopy(entries, 0, newEntries, 0, entries.length);
		System.arraycopy(classpathEntries, 0, newEntries, entries.length,
				classpathEntries.length);
		project.setRawClasspath(newEntries, monitor);
	}

	public static IClasspathEntry getClasspathEntry(IPackageFragmentRoot root)
			throws JavaModelException {
		IClasspathEntry rawEntry = root.getRawClasspathEntry();
		int rawEntryKind = rawEntry.getEntryKind();
		switch (rawEntryKind) {
		case IClasspathEntry.CPE_LIBRARY:
		case IClasspathEntry.CPE_VARIABLE:
		case IClasspathEntry.CPE_CONTAINER:
			if ((root.isArchive())
					&& (root.getKind() == IPackageFragmentRoot.K_BINARY)) {
				IClasspathEntry resolvedEntry = root
						.getResolvedClasspathEntry();
				if (resolvedEntry.getReferencingEntry() != null) {
					return resolvedEntry;
				}
				return rawEntry;
			}
			break;
		case IClasspathEntry.CPE_PROJECT:
		case IClasspathEntry.CPE_SOURCE:
		}
		return rawEntry;
	}

	public static String getPackageFragmentRootLabel(IPackageFragmentRoot root) {
		StringBuilder sb = new StringBuilder();
		if (appendVariableLabel(sb, root)) {
			return sb.toString();
		}
		if (root.isArchive()) {
			appendArchiveLabel(sb, root);
		} else {
			appendFolderLabel(sb, root);
		}
		return sb.toString();
	}

	private static void appendArchiveLabel(StringBuilder sb,
			IPackageFragmentRoot root) {
		if (root.isExternal()) {
			appendExternalArchiveLabel(sb, root);
		} else {
			sb.append(root.getPath().makeRelative().toString());
		}
	}

	private static void appendExternalArchiveLabel(StringBuilder sb,
			IPackageFragmentRoot root) {
		IPath path;
		try {
			IClasspathEntry classpathEntry = getClasspathEntry(root);
			IPath rawPath = classpathEntry.getPath();
			if ((classpathEntry.getEntryKind() != IClasspathEntry.CPE_CONTAINER)
					&& (!rawPath.isAbsolute())) {
				path = rawPath;
			} else {
				path = root.getPath();
			}
		} catch (JavaModelException localJavaModelException) {
			path = root.getPath();
		}
		sb.append(path.toOSString());
	}

	private static void appendFolderLabel(StringBuilder sb,
			IPackageFragmentRoot root) {
		if (root.getResource() == null) {
			appendExternalArchiveLabel(sb, root);
		} else {
			sb.append(root.getPath().makeRelative().toString());
		}
	}

	private static boolean appendVariableLabel(StringBuilder sb,
			IPackageFragmentRoot root) {
		try {
			IClasspathEntry rawEntry = root.getRawClasspathEntry();
			if (rawEntry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
				IClasspathEntry entry = getClasspathEntry(root);
				if (entry.getReferencingEntry() != null) {
					return false;
				}
				IPath path = rawEntry.getPath().makeRelative();
				sb.append(path.toString());
				sb.append(" - ");
				if (root.isExternal()) {
					sb.append(root.getPath().toOSString());
				} else {
					sb.append(root.getPath().makeRelative().toString());
				}
				return true;
			}
		} catch (JavaModelException localJavaModelException) {
			return false;
		}
		return false;
	}

	public static IPath getJREContainerPath(IProject project) {
		try {
			IJavaProject javaProject = getJavaProject(project);
			if (javaProject != null) {
				for (IClasspathEntry entry : javaProject.getRawClasspath()) {
					if (JavaRuntime.JRE_CONTAINER.equals(entry.getPath()
							.segment(0))) {
						return entry.getPath();
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return null;
	}

	public static IClasspathEntry createClasspathEntry(File path, File srcPath,
			IAccessRule[] rules) throws IOException {
		String p = path.getCanonicalPath();
		String sp = srcPath != null ? srcPath.getCanonicalPath() : null;
		String vp = withVariable(p);
		String vsp = sp != null ? withVariable(sp) : null;
		if (vp != null || vsp != null) {
			return JavaCore.newVariableEntry(new Path(vp != null ? vp : p),
					sp != null ? new Path(vsp != null ? vsp : sp) : null,
					sp != null ? new Path("") : null, rules, null, false);
		}
		return JavaCore.newLibraryEntry(new Path(p), sp != null ? new Path(sp)
				: null, sp != null ? new Path("") : null, rules, null, false);
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

	public static IAccessRule[] createForbiddenRules(String pattern) {
		return new IAccessRule[] { JavaCore.newAccessRule(new Path(pattern),
				IAccessRule.K_NON_ACCESSIBLE) };
	}
}
