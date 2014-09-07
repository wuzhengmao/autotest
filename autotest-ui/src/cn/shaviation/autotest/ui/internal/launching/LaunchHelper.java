package cn.shaviation.autotest.ui.internal.launching;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.ui.AutoTestUI;

public abstract class LaunchHelper {

	public static IResource getContext() {
		IWorkbenchWindow window = AutoTestUI.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window != null ? window.getActivePage() : null;
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (!ss.isEmpty()) {
					return LaunchHelper.getResource(ss.getFirstElement());
				}
			}
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				return (IResource) input.getAdapter(IResource.class);
			}
		}
		return null;
	}

	public static IResource getResource(Object element) {
		if (element instanceof IResource) {
			if (!(element instanceof IWorkspaceRoot)) {
				return (IResource) element;
			}
		} else if (element instanceof IJavaElement) {
			try {
				return ((IJavaElement) element).getCorrespondingResource();
			} catch (JavaModelException e) {
			}
		} else if (element instanceof IAdaptable) {
			return (IResource) ((IAdaptable) element)
					.getAdapter(IResource.class);
		}
		return null;
	}

	public static String getResourceLocation(IResource resource) {
		if (JavaUtils.isJavaProject(resource.getProject())) {
			if (resource instanceof IProject) {
				return "/";
			} else if (resource instanceof IFile) {
				if (AutoTestCore.TEST_SCRIPT_FILE_EXTENSION
						.equalsIgnoreCase(resource.getFileExtension())
						&& isSourceFolder(resource.getParent())) {
					return getPackagePath(resource.getParent())
							+ resource.getName();

				}
			} else if (resource instanceof IFolder) {
				if (isSourceFolder((IFolder) resource)) {
					return getPackagePath((IFolder) resource);
				}
			}
		}
		return null;
	}

	private static boolean isSourceFolder(IContainer folder) {
		IJavaElement javaElement = JavaCore.create(folder);
		if (javaElement != null
				&& ((javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) || (javaElement
						.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT))) {
			return true;
		}
		return false;
	}

	private static String getPackagePath(IContainer folder) {
		IJavaElement javaElement = JavaCore.create(folder);
		if (javaElement != null
				&& javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			return "/" + javaElement.getElementName().replace('.', '/') + "/";
		} else {
			return "/";
		}
	}

	public static IPath getJREContainerPath(IProject project) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
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
}
