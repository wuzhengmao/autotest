package cn.shaviation.autotest.core.jdt;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import cn.shaviation.autotest.util.Objects;

public abstract class NonJavaResourceFinder {

	public static void search(IJavaProject javaProject, String fileExtension,
			INonJavaResourceVisitor visitor, IProgressMonitor monitor)
			throws CoreException {
		IPackageFragmentRoot[] packageFragmentRoots = javaProject
				.getPackageFragmentRoots();
		if (monitor != null) {
			monitor.beginTask("Search...", packageFragmentRoots.length);
		}
		try {
			for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
				Object[] nonJavaResources = packageFragmentRoot
						.getNonJavaResources();
				IJavaElement[] packageFragments = packageFragmentRoot
						.getChildren();
				int tasks = nonJavaResources.length + packageFragments.length;
				IProgressMonitor subMonitor = null;
				if (monitor != null && tasks > 0) {
					subMonitor = new SubProgressMonitor(monitor, 1);
					subMonitor.beginTask(
							"Search " + packageFragmentRoot.getElementName()
									+ "...", tasks);
				}
				try {
					for (Object element : nonJavaResources) {
						if (!iterateElement("", element, fileExtension, visitor)) {
							return;
						}
						if (subMonitor != null) {
							subMonitor.worked(1);
						}
					}
					for (IJavaElement javaElement : packageFragments) {
						if (javaElement instanceof IPackageFragment) {
							IPackageFragment packageFragment = (IPackageFragment) javaElement;
							for (Object element : packageFragment
									.getNonJavaResources()) {
								String path = "/"
										+ packageFragment.getElementName()
												.replace('.', '/');
								if (!iterateElement(path, element,
										fileExtension, visitor)) {
									return;
								}
							}
						}
						if (subMonitor != null) {
							subMonitor.worked(1);
						}
					}
				} finally {
					if (subMonitor != null) {
						subMonitor.done();
					}
				}
				if (monitor != null) {
					monitor.worked(1);
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	private static boolean iterateElement(String path, Object element,
			String fileExtension, INonJavaResourceVisitor visitor)
			throws CoreException {
		if (element instanceof IResource) {
			if (!iterateResource(path, (IResource) element, fileExtension,
					visitor)) {
				return false;
			}
		} else if (element instanceof IJarEntryResource) {
			if (!iterateJarResource(path, (IJarEntryResource) element,
					fileExtension, visitor)) {
				return false;
			}
		}
		return true;
	}

	private static boolean iterateResource(String path, IResource resource,
			String fileExtension, INonJavaResourceVisitor visitor)
			throws CoreException {
		path += "/" + resource.getName();
		if (resource instanceof IFolder) {
			for (IResource member : ((IFolder) resource).members()) {
				if (!iterateResource(path, member, fileExtension, visitor)) {
					return false;
				}
			}
		} else if (resource instanceof IFile
				&& (fileExtension == null || fileExtension.equals(Objects
						.toString(resource.getFileExtension())))) {
			if (!visitor.visit(path, (IFile) resource)) {
				return false;
			}
		}
		return true;
	}

	private static boolean iterateJarResource(String path,
			IJarEntryResource resource, String fileExtension,
			INonJavaResourceVisitor visitor) throws CoreException {
		path += "/" + resource.getName();
		if (!resource.isFile()) {
			for (IJarEntryResource member : resource.getChildren()) {
				if (!iterateJarResource(path, member, fileExtension, visitor)) {
					return false;
				}
			}
		} else if (fileExtension == null
				|| fileExtension.equals(getFileExtension(resource))) {
			if (!visitor.visit(path, resource)) {
				return false;
			}
		}
		return true;
	}

	private static String getFileExtension(IJarEntryResource resource) {
		String name = resource.getName();
		int i = name.lastIndexOf('.');
		return i >= 0 ? name.substring(i + 1) : "";
	}

	public static Object lookup(IJavaProject javaProject,
			final String location, IProgressMonitor monitor)
			throws CoreException {
		final Object[] result = new Object[1];
		search(javaProject, null, new INonJavaResourceVisitor() {

			@Override
			public boolean visit(String path, IFile resource)
					throws CoreException {
				if (path.equals(location)) {
					result[0] = resource;
					return false;
				} else {
					return true;
				}
			}

			@Override
			public boolean visit(String path, IJarEntryResource resource)
					throws CoreException {
				if (path.equals(location)) {
					result[0] = resource;
					return false;
				} else {
					return true;
				}
			}
		}, monitor);
		return result[0];
	}
}
