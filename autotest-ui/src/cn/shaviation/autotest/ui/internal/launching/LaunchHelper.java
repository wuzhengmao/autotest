package cn.shaviation.autotest.ui.internal.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.jdt.AutoTestProjects;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.ui.AutoTestUI;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.util.Logs;

public abstract class LaunchHelper {

	public static Object getContext() {
		IWorkbenchWindow window = AutoTestUI.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window != null ? window.getActivePage() : null;
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (!ss.isEmpty()) {
					return getResource(ss.getFirstElement());
				}
			}
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				return getResource(part.getEditorInput());
			}
		}
		return null;
	}

	public static Object getResource(Object element) {
		if (element instanceof IResource) {
			if (!(element instanceof IWorkspaceRoot)) {
				return element;
			}
		} else if (element instanceof IJavaElement) {
			int type = ((IJavaElement) element).getElementType();
			if (type == IJavaElement.COMPILATION_UNIT
					|| type == IJavaElement.CLASS_FILE
					|| type == IJavaElement.PACKAGE_FRAGMENT
					|| type == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
				return element;
			} else {
				try {
					return ((IJavaElement) element).getUnderlyingResource();
				} catch (JavaModelException e) {
				}
			}
		} else if (element instanceof IJarEntryResource) {
			return element;
		} else if (element instanceof IStorageEditorInput) {
			try {
				return getResource(((IStorageEditorInput) element).getStorage());
			} catch (CoreException e) {
			}
		} else if (element instanceof IAdaptable) {
			Object resource = ((IAdaptable) element)
					.getAdapter(IResource.class);
			if (resource == null) {
				resource = ((IAdaptable) element)
						.getAdapter(IJavaElement.class);
			}
			return resource;
		}
		return null;
	}

	public static IProject getProject(Object resource) {
		if (resource instanceof IResource) {
			return ((IResource) resource).getProject();
		} else if (resource instanceof IJarEntryResource) {
			return ((IJarEntryResource) resource).getPackageFragmentRoot()
					.getJavaProject().getProject();
		} else if (resource instanceof IJavaElement) {
			return ((IJavaElement) resource).getJavaProject().getProject();
		} else {
			return null;
		}
	}

	public static IJavaProject getJavaProject(Object resource) {
		if (resource instanceof IResource) {
			return JavaUtils
					.getJavaProject(((IResource) resource).getProject());
		} else if (resource instanceof IJarEntryResource) {
			return ((IJarEntryResource) resource).getPackageFragmentRoot()
					.getJavaProject();
		} else if (resource instanceof IJavaElement) {
			return ((IJavaElement) resource).getJavaProject();
		} else {
			return null;
		}
	}

	public static String getResourceLocation(Object resource) {
		if (resource instanceof IResource) {
			return AutoTestProjects.getResourceLocation((IResource) resource,
					AutoTestCore.TEST_SCRIPT_FILE_EXTENSION, true);
		} else if (resource instanceof IJarEntryResource) {
			return AutoTestProjects.getResourceLocation(
					(IJarEntryResource) resource,
					AutoTestCore.TEST_SCRIPT_FILE_EXTENSION, true);
		} else if (resource instanceof IJavaElement) {
			return AutoTestProjects.getResourceLocation(
					(IJavaElement) resource,
					AutoTestCore.TEST_SCRIPT_FILE_EXTENSION, true);
		} else {
			return null;
		}
	}

	public static Action[] getLaunchActions(IEditorPart part) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry
				.getConfigurationElementsFor("org.eclipse.debug.ui.launchShortcuts");
		for (IConfigurationElement element : elements) {
			if (AutoTestUI.LAUNCH_SHORTCUT_ID
					.equals(element.getAttribute("id"))) {
				return new Action[] { createAction(element, part, "run"),
						createAction(element, part, "debug") };
			}
		}
		return new Action[0];
	}

	private static Action createAction(final IConfigurationElement element,
			final IEditorPart part, final String mode) {
		Action action = new Action(
				"debug".equals(mode) ? "Launch in Debug mode" : "Launch") {
			@Override
			public void run() {
				try {
					ILaunchShortcut shortcut = (ILaunchShortcut) element
							.createExecutableExtension("class");
					shortcut.launch(
							new StructuredSelection(part.getEditorInput()),
							mode);
				} catch (CoreException e) {
					Logs.e(e);
				}
			}
		};
		action.setToolTipText(action.getText());
		action.setImageDescriptor(UIUtils.getImageDescriptor(mode + "_exc.gif"));
		return action;
	}
}
