package cn.shaviation.autotest.core.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

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
}
