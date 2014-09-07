package cn.shaviation.autotest.core.internal.launching;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.jdt.INonJavaResourceVisitor;
import cn.shaviation.autotest.core.jdt.NonJavaResourceFinder;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.util.Logs;

public class AutoTestPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (!(receiver instanceof IAdaptable)) {
			throw new IllegalArgumentException(
					"Element must be of type 'IAdaptable', is " + receiver == null ? "null"
							: receiver.getClass().getName());
		}
		IResource resource;
		if ((receiver instanceof IResource)) {
			resource = (IResource) receiver;
		} else {
			resource = (IResource) ((IAdaptable) receiver)
					.getAdapter(IResource.class);
		}
		if (resource == null) {
			return false;
		} else if ("canLaunch".equals(property)) {
			return canLaunch(resource);
		}
		throw new IllegalArgumentException("Unknown test property '" + property
				+ "'");
	}

	private boolean canLaunch(IResource resource) {
		try {
			if (!JavaUtils.isJavaProject(resource.getProject())) {
				return false;
			} else if (!resource.getProject().hasNature(AutoTestCore.NATURE_ID)) {
				return false;
			} else if (resource instanceof IProject) {
				IJavaProject javaProject = JavaCore.create((IProject) resource);
				final boolean[] result = new boolean[] { false };
				NonJavaResourceFinder.search(javaProject,
						AutoTestCore.TEST_SCRIPT_FILE_EXTENSION,
						new INonJavaResourceVisitor() {

							@Override
							public boolean visit(String path, IFile resource)
									throws CoreException {
								result[0] = true;
								return false;
							}

							@Override
							public boolean visit(String path,
									IJarEntryResource resource)
									throws CoreException {
								result[0] = true;
								return false;
							}
						}, null);
				return result[0];
			} else {
				IResource folder = resource instanceof IFile ? resource
						.getParent() : resource;
				if (!(folder instanceof IFolder)) {
					return false;
				}
				IJavaElement javaElement = JavaCore.create(folder);
				if (javaElement == null
						|| ((javaElement.getElementType() != IJavaElement.PACKAGE_FRAGMENT) && (javaElement
								.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT))) {
					return false;
				}
				final boolean[] result = new boolean[] { false };
				resource.accept(new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource)
							throws CoreException {
						if (resource instanceof IFile
								&& AutoTestCore.TEST_SCRIPT_FILE_EXTENSION
										.equalsIgnoreCase(resource
												.getFileExtension())) {
							result[0] = true;
							return false;
						}
						return true;
					}
				});
				return result[0];
			}
		} catch (CoreException e) {
			Logs.e(e);
			return false;
		}
	}
}
